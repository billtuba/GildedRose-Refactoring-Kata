(ns gilded.core)

(def AGED_BRIE "Aged Brie")
(def BACKSTAGE_PASS "Backstage passes to a TAFKAL80ETC concert")
(def SULFURAS "Sulfuras, Hand of Ragnaros")



(defn make-store [items]
  (assert (vector? items))
  (->> items
       (map (fn [item] (atom item)))
       vec))

(defn item-seq [store]
  (->> store
       (map deref)))
;; ---

(defn incr-quality!* [item]
  (if (< (:quality item) 50)
    (update item :quality inc)
    item))

(defn dec-quality!* [item]
  (if (> (:quality item) 0)
    (update item :quality dec)
    item))

(defn expired?* [item]
  (->> item
       :sell-in
       neg?))

(defn update-quality [{:keys [name sell-in] :as it}]
  (cond->>   it
    (#{AGED_BRIE BACKSTAGE_PASS} name) incr-quality!*

    (and (= name BACKSTAGE_PASS)
         (< sell-in 10))
    incr-quality!*

    (and (= name BACKSTAGE_PASS)
         (< sell-in 5))
    incr-quality!*

    (not (#{AGED_BRIE BACKSTAGE_PASS SULFURAS} name))
    dec-quality!*))

(defn update-sell-in [pred it]
  (update it :sell-in
          (fn [v]
            (if pred
              v
              (dec v)))))

(defn update-expired [{:keys [name] :as it}]
  (if (expired?* it)
    (cond (= name AGED_BRIE)       (incr-quality!* it)
          (= name BACKSTAGE_PASS)  (assoc it :quality 0)
          (not (#{SULFURAS} name)) (dec-quality!*  it)
          :else  it)
    it))

(defn update-quality! [store]
  (doseq [item store]
    (swap! item (fn [{:keys [name] :as it}]
                  (->> it
                       (update-sell-in (#{SULFURAS} name))
                       update-quality
                       update-expired)))))

