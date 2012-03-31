(use 'clojure.java.io)
(use '[clojure.string :only [capitalize]])

(defn java-test-file? [file] (.matches (.getName file) ".*[Tt]est.*\\.java"))
(defn java-test-files [dirname]
  (filter java-test-file? (file-seq (as-file dirname))))

(defn extract-feature [line] (.replaceFirst line "(?s).*?void([^(]+).*" "-$1")) 

(defn test-lines [file]
  (map extract-feature (rest (.split (slurp file) "@Test"))))

(defrecord Testfile [name lines])
(defn make-test-file [file]
  (Testfile. (.getName file) (test-lines file)))

(defn as-header [filename]
  (str (.replaceFirst (.replaceFirst filename "\\.java" "") "Test" "") ":"))

(defn skipTestPerfixIfPresent [feature]
	(.replaceFirst feature "- test" "-"))

(defn formSentense [feature]
	(str " - " (capitalize (.substring feature 2)) ".") )
	
(defn as-feature [line]
  (formSentense (skipTestPerfixIfPresent (.toLowerCase (.replaceAll line "([A-Z])" " $1")))))

(def empty-line "")

(defn test-lines? [test-file] (not-empty (:lines test-file)))

(defn make-spec [test-file]
  (if (test-lines? test-file)
    [ 
      (as-header (:name test-file))
      (map as-feature (:lines test-file))
      empty-line]))

(dorun 
  (map (fn [s] (if s (println s)))
    (flatten
      (map make-spec
           (map make-test-file 
                (java-test-files "."))))))
