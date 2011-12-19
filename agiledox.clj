(use 'clojure.java.io)

(defn java-test-file? [file] (.matches (.getName file) ".*[Tt]est.*\\.java"))
(defn java-test-files [dirname]
  (filter java-test-file? (file-seq (as-file dirname))))

(defn extract-feature [line] (.replaceFirst line "(?s).*?void([^(]+).*" "-$1")) 

(defn test-lines [file]
  (map extract-feature (rest (.split (slurp file) "@Test"))))

(defstruct test-file :name :lines)
(defn make-test-file [file]
  (struct test-file (.getName file) (test-lines file)))

(defn as-header [filename]
  (.replaceFirst (.replaceFirst filename "\\.java" "") "Test" ""))

(defn as-feature [line]
  (.toLowerCase (.replaceAll line "([A-Z])" " $1")))

(defn test-lines? [test-file] (not-empty (test-file :lines)))
(defn print-class [test-file] (println (as-header (test-file :name))))
(defn print-line [line] (println (as-feature line)))
(defn print-lines [test-file]
  (dorun
    (map print-line
         (test-file :lines))))

(defn print-spec [test-file]
  (if (test-lines? test-file)
    (do
      (print-class test-file)
      (print-lines test-file)
      (println ""))))

(dorun 
  (map print-spec 
       (map make-test-file 
            (java-test-files "."))))
