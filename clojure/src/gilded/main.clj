(ns gilded.main
  (:require
   [clojure.string :as str]
   [gilded.core :as core]))

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

(def default-items
  [{:name "+5 Dexterity Vest"                          :quality 20  :sell-in 10}
   {:name "Aged Brie"                                  :quality  0  :sell-in  2}
   {:name "Elixir of the Mongoose"                     :quality  7  :sell-in  5}
   {:name "Sulfuras  Hand of Ragnaros"                 :quality 80  :sell-in  0}
   {:name "Sulfuras  Hand of Ragnaros"                 :quality 80  :sell-in -1}
   {:name "Backstage passes to a TAFKAL80ETC concert"  :quality 20  :sell-in 15}
   {:name "Backstage passes to a TAFKAL80ETC concert"  :quality 49  :sell-in 10}
   {:name "Backstage passes to a TAFKAL80ETC concert"  :quality 49  :sell-in  5}
   {:name "Conjured Mana Cake"                         :quality  6  :sell-in  3}])

(defn- format-report [[day rpt]]
  (str day "\n" rpt "\n"))

(defn- report-by-day [day lines]
  [(header day)
   (body lines)])

(def default-length-of-report "2")

(defn- parse-args [[n-days items]]
  {:n-days (parse-long (or n-days default-length-of-report))
   :items (or items default-items)})

(defn- create-report
  [n-days items]
  (->> items
       core/update-items
       (map-indexed (comp
                     format-report
                     report-by-day))
       (take n-days)
       (str/join "")))

(defn -main [& args]
  (let [{:keys [n-days items]} (parse-args args)
        the-report (create-report n-days items)]
    (print the-report)))
