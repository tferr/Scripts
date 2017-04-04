; BARlib.clj
; IJ BAR: https://github.com/tferr/Scripts#scripts
;
; Common BAR library (https://github.com/tferr/Scripts/tree/master/lib#lib) to be
; placed in BAR/lib. This file can host functions to be used across your scripts.
; To load these scripting additions, append the following to your Clojure files:
;
;     (load-file (str (bar.Utils/getLibDir) "BARlib.clj"))
;
; Then, call functions as usual:
;     (confirmLoading)

(import '(org.apache.commons.math3.util ArithmeticUtils))

;;;;;;;;;;;  UTILITIES  ;;;;;;;;;;;
(defn confirmLoading []
  "Acknowledges accessibility to this file"
  (ij.IJ/showMessage "BAR lib successfully loaded!"))

(defn getClipboardText []
  "Returns text from the system clipboard or an empty string if no text was found"
  (bar.Utils/getClipboardText))

(defn randomString []
  "Returns a random uuid"
  (str (java.util.UUID/randomUUID)))


;;;;;;;;;;;  CALCULATIONS  ;;;;;;;;;;;
(defn gcd [a b]
  "Returns the greatest common divisor between 2 numbers"
  (if (= b 0)
    a
  (recur b (rem a b))))

(defn gcdCommons [a b]
  "Returns the greatest common divisor between 2 numbers using Commons Math"
  (ArithmeticUtils/gcd a b))

(defn sphereCalc [r]
  "Returns surface area and volume of a sphere of radius r"
  (let [sph_area (double (* 4 Math/PI (Math/pow r 2))),
        sph_vol (double (/ (* 4 Math/PI (Math/pow r 3)) 3))]
  [sph_area, sph_vol]))
