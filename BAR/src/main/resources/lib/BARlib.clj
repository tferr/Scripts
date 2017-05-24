;@UIService ui

; BARlib.clj
; IJ BAR: https://github.com/tferr/Scripts
;
; Template BAR library (http://imagej.net/BAR#BAR_lib) to be placed in BAR/lib. This file
; demonstrates how functions/methods in a common file can be shared across your scripts.
; To load such scripting additions, append the following to your Clojure files:
;
;     (load-file (str (bar.Utils/getLibDir) "BARlib.clj"))
;
; Then, call functions as usual:
;     (confirmLoading)
;
; (See resources in BAR>Help for more details)

(import '(org.apache.commons.math3.util ArithmeticUtils))

(defn confirmLoading []
  "Acknowledges accessibility to this file"
  (uiservice/showDialog "BAR lib successfully loaded!"))

(defn getClipboardText []
  "Returns text from the system clipboard or an empty string if no text was found"
  (bar.Utils/getClipboardText))

(defn randomString []
  "Returns a random uuid"
  (str (java.util.UUID/randomUUID)))

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
