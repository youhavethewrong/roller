(ns roller.core
  (:require [clojure.java.io :as io])
  (:gen-class))

(defn find-all-files
  [f]
  (if (and (.isFile f) (not (.isHidden f)))
    f
    (flatten (map #(find-all-files %) (.listFiles f)))))

(defn filter-files
  [dirlist dest]
  (filter #(and (.contains (.getName %) ".org")
                (not=(.getName %) dest)
                (not (.contains (.getAbsolutePath %) ".git"))) dirlist))

(defn sort-files
  [dirlist]
  (sort #(compare (.getAbsolutePath %1) (.getAbsolutePath %2)) dirlist))

(defn append-all
  [dirlist dest]
  (with-open [w (io/writer dest)]
    (doseq [f dirlist]
      (with-open [r (io/reader f)]
        (.append w (str "* " (.getPath f) "\n"))
        (doseq [l (line-seq r)]
          (if (.startsWith l "*")
            (.append w (str "*" l "\n"))
            (.append w (str l "\n"))))))))

(defn -main
  [& args]
  "Roll all emacs org files below a point into a single destination file."
  (if (= 2 (count args))
    (let [src (io/file (first args))
          dest (second args)]
      (-> (find-all-files src)
          (filter-files dest)
          (sort-files)
          (append-all dest)))
    (println "Usage: java -jar roller.jar source-directory destination-file")))
