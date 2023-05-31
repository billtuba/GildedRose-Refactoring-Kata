(ns gilded.main-test
  (:require [gilded.main :as sut]
            [clojure.test :as t]))

(t/deftest gold-test
  (let [actual (with-out-str (sut/-main))]
    (t/is (= (slurp "gold.txt") actual))))
