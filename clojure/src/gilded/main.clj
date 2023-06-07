(ns gilded.main
  (:require
   [clojure.string :as str]
   [gilded.core :as x]))

(defn- report-line [{:keys [name sell-in quality] :as _item}]
  (str name ", " sell-in ", " quality))

(defn- body-content [lines]
  (let [header ["name, sellIn, quality"]
        body   (mapv report-line lines)
        footer [nil]]
    (concat header body footer)))

(defn- header [day]
  (str "-------- day " day " --------"))

(defn- body [lines]
  (->> lines
       body-content
       (str/join "\n")))

(def fixture
  [{:name "+5 Dexterity Vest"                          :quality 20  :sell-in 10}
   {:name "Aged Brie"                                  :quality  0  :sell-in  2}
   {:name "Elixir of the Mongoose"                     :quality  7  :sell-in  5}
   {:name "Sulfuras  Hand of Ragnaros"                 :quality 80  :sell-in  0}
   {:name "Sulfuras  Hand of Ragnaros"                 :quality 80  :sell-in -1}
   {:name "Backstage passes to a TAFKAL80ETC concert"  :quality 20  :sell-in 15}
   {:name "Backstage passes to a TAFKAL80ETC concert"  :quality 49  :sell-in 10}
   {:name "Backstage passes to a TAFKAL80ETC concert"  :quality 49  :sell-in  5}
   {:name "Conjured Mana Cake"                         :quality  6  :sell-in  3}])

(defn -main [& args]
  (let [n-days (if (nil? (first args))
                 2
                 (Long/parseLong (first args)))
        store (x/make-store fixture)]
    (dotimes [day n-days]
      (let [lines (x/item-seq store)
            report (str (header day)
                        "\n"
                        (body lines))]
        (println report)
        (x/update-quality! store)))))
