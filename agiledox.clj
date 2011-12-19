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

(defn make-spec [test-file]
  (if (test-lines? test-file)
    (list
      (as-header (test-file :name))
      (map as-feature (test-file :lines))
      "")))

(dorun 
  (map (fn [s] (if s (println s)))
    (flatten
      (map make-spec
           (map make-test-file 
                (java-test-files "."))))))
