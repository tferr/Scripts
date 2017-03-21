#@File(label="MtrackJ 'Points' table") table_file
#@Boolean(label="Auto-detect frame interval", value=True) detect_frame_rate
#@UIService uiservice
#@LogService lservice

'''
Tag_and_Onset_MtrackJ_paths.py
https://github.com/tferr/Scripts/

Parses MTrackJ's 'detailed' table and does two things:
1) Appends a flag colum to the parsed table spliting paths into 'lingering',
   'elongating' and 'retracting' bouts
2) Creates a new table (on the same directory of parsed file) identifing
   the first 'non-lingering' time frame of a path.

TF v1.0.0 2017.03.21
'''

from ij.measure import ResultsTable as RT
import os, math


'''
How many consecutive path points should be used to define a bout? eg, a
BOUT_WINDOW of 2 means 4 consecutive tracking points (current position ± 2)
'''
BOUT_WINDOW = 2  # 4 consecutive tracking points

'''
Min. distance between consecutive track points that defines lingering. Any
smaller distance than this value will be considered as 'lingering' if it
lasts for the number of consecutive tracking points defined by BOUT_WINDOW
'''
MIN_D2P = math.sqrt(0.2116403 ** 2)  # diagonal of pixel size for full sensor CCD at 20X

''' Default frame interval (seconds). Ignored when auto-detection is active'''
DEF_FRAME_INTERVAL = 29.73

''' Display progress and debug info to console? '''
debug = True



def getColumnHeadings(rt):
    headings = rt.getColumnHeadings()
    if "\t" in headings:
        headings = headings.split("\t")
    else:
        headings = headings.split(",")
    # Annoyingly, if tables are imported with row numbers, all column
    # indices seem to be shifted by one. In those cases, we'll get rid
    # of the first row number column heading containing just a whitespace
    # rt.showRowNumbers(False)
    if not headings[0].strip():
        log("Headings: %s" % headings)
        log("Deleting first column heading '%s'" % headings[0])
        del headings[0]
    return headings


def getColumnIndex(headings, substring):
    for i, s in enumerate(headings):
        if substring in s:
            log("Column index for %s: %s" % (s.decode("utf-8", "ignore"), i))
            return i
    return RT.COLUMN_NOT_FOUND


def isNumber(a):
    return not math.isnan(a)


def log(*arg):
    if (debug): lservice.info("%s" % ''.join(map(str, arg)))


def suffixed_path(filepath, suffix):
    n_path, n_ext = os.path.splitext(filepath)
    return "{path}_{suffix}{ext}".format(path=n_path, suffix=suffix, ext=n_ext)


def getFrameInterval(row_indices, id_rows, t_rows):
    frame_interval = 99999999999999999
    for row, next_row in zip(row_indices, row_indices[1:]):
        if (id_rows[next_row] == id_rows[row]):
            t = t_rows[next_row] - t_rows[row]
            if t < frame_interval:
                frame_interval = t
    log("Detected frame rate: %s" % frame_interval)
    return frame_interval


def main():
    rt = RT.open2(table_file.getAbsolutePath())
    if not rt: return

    log(" --- --- --- ")
    log("Loaded %s" % table_file.getAbsolutePath())
    log("Loading column lists...")

    # Get column indices from imported file
    headings = getColumnHeadings(rt)
    id_col = getColumnIndex(headings, "TID")
    t_col = getColumnIndex(headings, "t [")
    d2p_col = getColumnIndex(headings, "D2P [")
    angle_col = getColumnIndex(headings, u'\u03B1 [deg]')
    delta_col = getColumnIndex(headings, u'\u0394\u03B1 [deg]')
    if angle_col == RT.COLUMN_NOT_FOUND:
        log("Failed to detect index for angle column. Re-trying...")
        angle_col = getColumnIndex(headings, u'? [deg]')
    if delta_col == RT.COLUMN_NOT_FOUND:
        log("Failed to detect index for delta angle column. Re-trying...")
        delta_col = getColumnIndex(headings, u'?? [deg]')
    log("Last column index is %s" % rt.getLastColumn())

    if RT.COLUMN_NOT_FOUND in (id_col, d2p_col, delta_col, angle_col):
        uiservice.showDialog("Error: Some key columns were not found!", "Invalid Table?")
        return

    # Store all data on dedicated lists
    track_id_rows = rt.getColumnAsDoubles(id_col)
    d2p_rows = rt.getColumnAsDoubles(d2p_col)
    angle_rows = rt.getColumnAsDoubles(angle_col)
    delta_rows = rt.getColumnAsDoubles(delta_col)
    t_rows = rt.getColumnAsDoubles(t_col)

    # Assess n of data points and extract unique path ids
    n_rows = len(track_id_rows)
    row_indices = range(n_rows)
    track_ids = set(track_id_rows)
    n_tracks = len(track_ids)
    log("Table has %g rows" % n_rows)
    log("Table has %g tracks" % n_tracks)

    log("Parsing tracks...")
    for track_id in track_ids:


        for row, next_row in zip(row_indices, row_indices[1:]):

            if track_id_rows[row] != track_id:
                continue

            if not isNumber(angle_rows[row]):
                 rt.setValue("FLAG", row, "NA")
                 continue

            lower_bound = max(0, row - BOUT_WINDOW + 1)
            upper_bound = min(n_rows-1, row + BOUT_WINDOW)
            win_d2p = []
            for win_row in range(lower_bound, upper_bound):
                win_d2p.append(d2p_rows[win_row])

            if sum(win_d2p) <= MIN_D2P * len(win_d2p):
                rt.setValue("FLAG", row, 0)

            else:
                current_angle = angle_rows[row]
                next_angle = angle_rows[next_row]
                current_delta = delta_rows[row]

                flag = -1 if current_angle < 0 else 1
                delta_change = (abs(current_delta) > 90)
                same_sign = ((current_angle<0) == (next_angle<0))
                if delta_change and not same_sign:
                    flag *= -1

                rt.setValue("FLAG", row, flag)
                if next_row == n_rows - 1:
                    rt.setValue("FLAG", next_row, flag)

    if rt.save(table_file.getAbsolutePath()):
        log("Processed table successfully saved (file overwritten)")
    else:
        log("Could not override input file. Displaying it...")
        rt.show(table_file.name)


    log("Creating onset table...")
    onset_rt = RT()
    onset_rt.showRowNumbers(False)

    frame_int = getFrameInterval(row_indices, track_id_rows, t_rows) if detect_frame_rate else DEF_FRAME_INTERVAL

    for track_id in track_ids:

        for row, next_row in zip(row_indices, row_indices[1:]):

            if track_id_rows[row] != track_id:
                continue

            flag = rt.getValue("FLAG", row)
            if not isNumber(flag):
                continue

            flag = int(flag)
            if flag == 0:
                continue

            if flag == 1 or flag == -1:
                srow = onset_rt.getCounter()
                onset_rt.incrementCounter()
                onset_rt.setValue("TID", srow, track_id)
                frame = int(t_rows[row]/frame_int) + 1
                frange = "%s to %s" % (frame, (frame + 1))
                onset_rt.setValue("1st non-lingering frame", srow, frange)
                onset_rt.setValue("ManualTag", srow, "")
                break

    out_path = suffixed_path(table_file.getAbsolutePath(), "ManualTagging")
    if onset_rt.save(out_path):
        log("Summary table successfully saved: %s" % out_path)
    else:
        log("File not saved... Displaying onset table")
        onset_rt.show("Onsets %s" % table_file.name)


if __name__ == '__main__':
    main()
