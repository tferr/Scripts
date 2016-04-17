# Extract_Bouts_From_Tracks.py
# https://github.com/tferr/Scripts/
#
# Jython script for ImageJ1 that segregates videotracked paths into "Moving" and "Motionless"
# bouts according to predefined spatial and temporal constraints, providing a visual description
# of the analysis.
#
# Requirements: An updated ImageJ distribution subscribed to the BAR update site (image.net/BAR).
# The script prompts for a single file of tabular tracked data (file can also be read from the
# clipboard), organized according to the following layout (the default produced  by TrackMate):
#
#    ID_HEADING | X_POS_HEADING | Y_POS_HEADING | T_POS_HEADING
#      track1   |       x1      |       y1      |       t1
#      track1   |       x2      |       y2      |       t2
#      track1   |       ...     |       ...     |       ...
#      track2   |       x1      |       y1      |       t1
#      track2   |       x2      |       y2      |       t2
#      track2   |       ...     |       ...     |       ...
#
# (Note that the file may contain other interleaved columns: those will be ignored).
# Parameters and settings are described below. Code annotations provide further details.
# TF 201512

### General Parameters
X_POS_HEADING = "POSITION_X"    # Column headings for x,y,t positions and heading
Y_POS_HEADING = "POSITION_Y"    # identifying individual tracks. TrackMate's
T_POS_HEADING = "FRAME"         # defaults are "POSITION_X", "POSITION_Y",
ID_HEADING = "TRACK_ID"         # "FRAME" and "TRACK_ID"

debug = True                    # Verbose mode?
displayDetailedTable = False    # Should the "detailed" table be displayed?
displayRasterTable = False      # Display the "rasterized tracks" table?
hideShortTracks = True          # Skip tracks shorter than the specified bout duration?
frameCal = (0.05, "min")        # Frame interval and respective unit


### Settings for bout-splitted track image
generateRasterTracks = True # Generate "tracks image"
shortestRasterTrack = 400   # Smallest track (in terms of duration) to be displayed
longestRasterTrack = 1800   # Longest track (in terms of duration) to be displayed
backgroundColor = 255       # Background color of image (8-bit gray scale) (white)
restingBoutColor = 77       # Color of resting bouts (8-bit gray scale) (30% gray)
movingBoutColor = 204       # Color of moving bouts (8-bit gray scale) (80% gray)
borderColor = 0             # Color of track border (8-bit gray scale) (black)


### Bout definitions
""" Max. speed below which a "resting" object is allowed to "move".
Objects moving less than this distance/frame will be tagged as "stopped"
"""
restingVelocity = 3

""" Moving window (in frames) defining the shortest duration of a displacement
bout. Each position in the track will be tagged with a binary motion flag
("Resting"=0; "Moving"=1). We'll monitor the amount of flag switches around each
frame by summing motion flags in its neighborhood. This will allow us to ignore
sporadic flag stitches and determine the overall displacement trend in each group
of frames. Note that the shortest possible bout in a neighborhood of 1 is 1 frame;
neighborhood of 2, 3 frames; neighborhood of 3, 5 frames, etc.
"""
neighborhood = 50

""" Binary flags for "resting" (motionless) and "moving" displacementes """
RESTING_FLAG, MOVING_FLAG = 0, 1



import math, sys, time
from ij import IJ, ImagePlus
from ij.measure import ResultsTable as RT
from ij.process import ImageProcessor
from bar import Utils

def new_Table():
    """Creates a new ResultsTable padded with float("nan"), ie, all
       empty cells will be filled with Java's String.valueOf(Double.NaN)
    """
    new_rt = RT()
    new_rt.setNaNEmptyCells(True);
    new_rt.showRowNumbers(False);
    return new_rt

def isNumber(a):
    """Clumsy check to determine if a ResultsTable cell holds valid data"""
    try:
        float(a)
        if str(a).lower()=="nan":
            return False
    except ValueError:
        return False
    return True

def nanToZero(value):
    """Converts float("nan") to zero"""
    if value!=value:value=0 #nan is never equal to nan
    return value

def findLastNonNumberIdx(collection):
    """Retrieves the index of the last numeric value in a list"""
    for r_idx, element in enumerate(reversed(collection)):
        if isNumber(element):
            return len(collection) - 1 - r_idx

def colorizeFlag(movement_flag):
    """Maps movement flags to grayscale values"""
    global MOVING_FLAG, RESTING_FLAG
    global backgroundColor, borderColor, restingBoutColor, movingBoutColor
    if not isNumber(movement_flag):
        raster_flag_color = backgroundColor # Color of float("nan")
    elif movement_flag==MOVING_FLAG:
        raster_flag_color = movingBoutColor
    else:
        raster_flag_color = restingBoutColor
    return raster_flag_color

def paintNaNpixels(ip, value):
    """Replaces NaN pixels in the specified ImageProcessor with
    the specified value"""
    for y in xrange(ip.getHeight()):
        for x in xrange(ip.getWidth()):
            if not isNumber(ip.getPixelValue(x,y)):
                ip.putPixelValue(x, y, value)

def log(*arg):
    """Convenience print call"""
    global debug
    if (debug): print(">>> %s" % ''.join(map(str, arg)))

def getColumn(table, heading):
    """Convenience function to retrieve a ResultsTable column from its heading string"""
    return table.getColumn(table.getColumnIndex(heading))

try:
    # Retrieve valid data
    rt = Utils.getTable();
    start = time.time()

    # Retrive x,y,t positions (all in unc. units)
    x = getColumn(rt, X_POS_HEADING)
    y = getColumn(rt, Y_POS_HEADING)
    t = getColumn(rt, T_POS_HEADING)

    # Retrieve the total n. of tracks
    track_ids = getColumn(rt, ID_HEADING)
    track_ids = [int(i) for i in track_ids]
    n_tracks = track_ids[-1]
    log("Tracks to be analyzed: ", n_tracks)
except:
    IJ.error("Invalid Results Table")

# Create "nan"-padded tables to hold results
detail_rt = new_Table()

# Extract indivifual tracks and determine the track with the
# largest data (i.e., the one with the highest number of rows)
track_row = 0
max_track_row = 0
for i in range(0, rt.getCounter()-1):

    track_label = str(track_ids[i])
    if (track_ids[i]==track_ids[i+1]):
        dx = (x[i+1]-x[i])**2
        dy = (y[i+1]-y[1])**2
        dt = t[i+1]-t[i]
        dis = math.sqrt(dx+dy)
        vel = dis/dt
        if (track_row>max_track_row):
            max_track_row = track_row

        # Log to "detailed" table
        if (i<=max_track_row):
            detail_rt.incrementCounter()
        detail_rt.setValue("Dis_" + track_label, track_row, dis)
        detail_rt.setValue("Vel_" + track_label, track_row, vel)
        detail_rt.setValue("Dur_" + track_label, track_row, dt)
        detail_rt.setValue("Flag_" + track_label, track_row,
            RESTING_FLAG if vel < restingVelocity else MOVING_FLAG)
        track_row += 1
    else:
        # Analyzed track just ended: Reset loop variables and create column
        # to hold bout flags
        track_row = 0
        detail_rt.setValue("BoutFlag_" + track_label, 0, float("nan"))
        detail_rt.setValue("Mov_Dur_" + track_label, 0, float("nan"))
        detail_rt.setValue("Rest_Dur_" + track_label, 0, float("nan"))
        log("Extracting track ", track_label)


listOfRasterPaths = [] # List holding raster tracks

# Loop through individual tracks and tag each datapoint (i.e., each row)
for track in range(0, n_tracks):

    durHeading = "Dur_" + str(track)
    fFlagHeading = "Flag_" + str(track)
    bFlagHeading = "BoutFlag_" + str(track)
    mDurHeading = "Mov_Dur_" + str(track)
    rDurHeading = "Rest_Dur_" + str(track)

    durations  = getColumn(detail_rt, durHeading)
    fFlags = getColumn(detail_rt, fFlagHeading)
    bFlags = getColumn(detail_rt, bFlagHeading)
    nDataPoints = findLastNonNumberIdx(durations) + 1

    log("Tagging track ", track, ": ", nDataPoints , " positions")
    for row in range(0, nDataPoints):

        # Define the boundaries of the moving window. "Stopping flags"
        # within this window will be monitoried to define a motionless bout
        # NB: Boundaries are defined from the rows of the input table. This
        # works only when the time elapsed betwen two rows is a single frame.
        # So we'll have to monitor the actual time that has elapsed within the
        # bounderies of the window
        lower_bound = max(0, row - neighborhood + 1)
        upper_bound = min(nDataPoints, row+neighborhood)
        sum_of_flags = 0
        sum_of_frames = 0
        neighborhood_sum = upper_bound - lower_bound

        for i in xrange(lower_bound, upper_bound):
            if isNumber(durations[i]) and isNumber(fFlags[i]):
                sum_of_flags += (fFlags[i] * durations[i])
                sum_of_frames += durations[i]
            if sum_of_frames >= neighborhood_sum:
                break

        # Assign this tracked point to its bout
        moving_bout_duration = float("nan")
        resting_bout_duration = float("nan")
        bout_flag = float("nan")
        if sum_of_flags >= neighborhood_sum:
            bout_flag = MOVING_FLAG
            moving_bout_duration = durations[row]
        else:
            bout_flag = RESTING_FLAG
            resting_bout_duration = durations[row]
        detail_rt.setValue(bFlagHeading, row, bout_flag)
        detail_rt.setValue(mDurHeading, row, moving_bout_duration)
        detail_rt.setValue(rDurHeading, row, resting_bout_duration)

    if generateRasterTracks:
        # Generate raster column if path is long enough
        if nDataPoints > shortestRasterTrack:

            # Retrieve updated column of bout flags
            bFlags = getColumn(detail_rt, bFlagHeading)

            # Generate raster column (motion-flags temporally aligned, all 1
            # frame apart) until the path duration reaches the maximum limit
            keepGrowingRasterPath = True
            for idx, duration in enumerate(durations):
                if (keepGrowingRasterPath):
                    flag = bFlags[idx]
                    for insertIdx in range(1, int(duration)):
                        if (len(bFlags)==longestRasterTrack):
                            keepGrowingRasterPath = False
                            break
                        bFlags.insert(idx+insertIdx, flag)

            # Store only lists without NaN values
            listOfRasterPaths.append(bFlags[:findLastNonNumberIdx(bFlags)])

    # Allow analysis to be interrupted
    if IJ.escapePressed():
        break

# Display table. Displaying it now may ensure all tracks are padded with "NaN"
if (displayDetailedTable):
    detail_rt.show("Track_Details["+ str(restingVelocity) +"-"+ str(neighborhood) +"]")


# Now that all paths are contained in listOfRasterPaths. Sort them by length of track
listOfRasterPaths = sorted(listOfRasterPaths, key = len)

# Create Image of analysis. We'll create it from a ResultsTable. It would be much
# more efficient to generate a text image directly, but this allows the table to be
# processed elsewhere if needed. In IJ1, column headings of a ResultsTable must be
# unique, so we will use distinct identifiers
if generateRasterTracks:
    raster_rt = new_Table()
    log('Tracks to be rendered:', len(listOfRasterPaths))
    for rasterPath in xrange(len(listOfRasterPaths)):

        log("Rendering track ", rasterPath)
        for row, raster_flag in enumerate(listOfRasterPaths[rasterPath]):

            if not isNumber(raster_flag):
                break
            if (row>raster_rt.getCounter()-1):
                raster_rt.incrementCounter()

            # Create upper border: 1 px-wide
            bColor = borderColor if isNumber(raster_flag) else backgroundColor
            raster_rt.setValue("Delim1_" + str(rasterPath), row, bColor)

            # Create raster path: 18 px wide
            raster_flag_color = colorizeFlag(raster_flag)
            for i in 'abcdefghijklmnopq':
                raster_rt.setValue("Raster_" + str(rasterPath) + str(i), row, raster_flag_color)

            # Create lower border: 1 px-wide
            raster_rt.setValue("Delim2_" + str(rasterPath), row, bColor)

            # Append padding space between tracks: 10px wide
            for j in 'abcdefghij':
                raster_rt.setValue("Space_" + str(rasterPath) + str(j), row, backgroundColor)

        # Allow analysis to be interrupted
        if IJ.escapePressed():
            break

    # Display table of rasterized tracks
    if displayRasterTable:
        raster_rt.show("RasterTracks["+ str(restingVelocity) +"-"+ str(neighborhood ) +"]")

    # Display image of rasterized tracks
    ip = raster_rt.getTableAsImage().rotateLeft()
    paintNaNpixels(ip, backgroundColor)
    ip = ip.convertToByte(False)
    imp = ImagePlus("RasterTracks["+ str(restingVelocity) +"-"+ str(neighborhood ) +"]", ip)
    imp.show()

    ## Add scale-bar for time
    IJ.run(imp, "Set Scale...", "distance=1 known="+ str(frameCal[0]) +" unit="+ frameCal[1]);
    IJ.run(imp, "Scale Bar...", "width=10 color=Black location=[Lower Right] overlay");


# Loop through individual tracks and extract some basic statistics. Most of these parameters
# are already retrieved by Trackmate. We calculate them here just for convenience
track_ids = []      # List holding the track identifier
sum_distances = []  # List holding the track's total distance
sum_durations = []  # List holding the track's total duration
max_speeds = []     # List holding the track's Max speed
min_speeds = []     # List holding the track's Min speed
sum_n_rests = []    # List holding the number of resting bouts in each track
sum_n_moves = []    # List holding the number of moving bouts in each track
sum_dur_rests = []  # List holding the total resting time of each tracked object
sum_dur_moves = []  # List holding the total moving time of each tracked object


log("Logging Summaries...")
summary_rt = new_Table()
for track in range(0, n_tracks):

    # Retrieve and store the track identifier
    track_id = str(track)
    track_ids.insert(track, "Track_"+track_id)

    # Retrive tracking data
    distances = getColumn(detail_rt, "Dis_" + track_id)
    durations = getColumn(detail_rt, "Dur_" + track_id)
    velocities = getColumn(detail_rt, "Vel_" + track_id)
    mov_durations = getColumn(detail_rt, "Mov_Dur_" + track_id)
    rest_durations = getColumn(detail_rt, "Rest_Dur_" + track_id)

    # Reset stats for this track
    track_sum_dis = 0
    track_sum_dur = 0
    track_max_vel = 0
    track_min_vel = sys.maxint
    track_sum_move = 0
    track_sum_rest = 0
    track_n_moves  = 0
    track_n_rests  = 0

    # Compute basic stats and store them in dedicated lists
    nDataPoints = findLastNonNumberIdx(distances) + 1
    for row in xrange(nDataPoints):
        track_sum_dis += distances[row]
        track_sum_dur += durations[row]
        if (velocities[row]>track_max_vel):
            track_max_vel = velocities[row]
        if (velocities[row]<track_min_vel):
            track_min_vel = velocities[row]
        if isNumber(mov_durations[row]):
            track_sum_move += mov_durations[row]
        if isNumber(rest_durations[row]):
            track_sum_rest += rest_durations[row]

    sum_distances.insert(track, track_sum_dis)
    sum_durations.insert(track, track_sum_dur)
    max_speeds.insert(track, track_max_vel)
    min_speeds.insert(track, track_min_vel)
    sum_dur_moves.insert(track, track_sum_move)
    sum_dur_rests.insert(track, track_sum_rest)

    # Assess the number of moving/resting bouts in this track
    for row in xrange(nDataPoints-1):
        if isNumber(mov_durations[row]) and not isNumber(mov_durations[row+1]):
            track_n_moves += 1
        if isNumber(rest_durations[row]) and not isNumber(rest_durations[row+1]):
            track_n_rests += 1

    # Predict cases in which bouts lasted entire track duration
    if track_n_moves==0 and track_sum_dur==track_sum_move:
        track_n_moves += 1
    if track_n_rests==0 and track_sum_dur==track_sum_rest:
        track_n_rests += 1

    sum_n_moves.insert(track, track_n_moves)
    sum_n_rests.insert(track, track_n_rests)

# Log summary data
for i in range(0, n_tracks):

    # Ignore tracks shorter than neighborhood
    if hideShortTracks and sum_durations[i]<neighborhood:
        continue

    row = summary_rt.getCounter()
    summary_rt.incrementCounter()
    summary_rt.setLabel(track_ids[i], row)
    summary_rt.setValue("Total dx", row, sum_distances[i])
    summary_rt.setValue("Duration", row, sum_durations[i])
    summary_rt.setValue("Max speed", row, max_speeds[i])
    summary_rt.setValue("Min speed", row, min_speeds[i])
    summary_rt.setValue("Moving dur", row, sum_dur_moves[i])
    summary_rt.setValue("Resting dur", row, sum_dur_rests[i])
    summary_rt.setValue("Resting %", row, 100 * sum_dur_rests[i] / sum_durations[i])
    summary_rt.setValue("Moving bouts", row, sum_n_moves[i])
    summary_rt.setValue("Resting bouts", row, sum_n_rests[i])
    if sum_n_moves[i]!=0:
        summary_rt.setValue("Avg moving bout dur", row, sum_dur_moves[i] / sum_n_moves[i])
    if sum_n_rests[i]!=0:
        summary_rt.setValue("Avg resting bout dur", row, sum_dur_rests[i] / sum_n_rests[i])

summary_rt.show("Track_Summaries["+ str(restingVelocity) +"-"+ str(neighborhood) +"]")
log("Finished: ", time.time()-start, " seconds")
