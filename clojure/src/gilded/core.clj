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

(defn incr-quality [item]
  (if (< (:quality item) 50)
    (update item :quality inc)
    item))

(defn hyper-quality [{:keys [sell-in] :as  item}]
  (cond->>
   (incr-quality item)
    (< sell-in 10) incr-quality
    (< sell-in 5)  incr-quality))

(defn dec-quality [item]
  (if (> (:quality item) 0)
    (update item :quality dec)
    item))

(defn dec-sell-in [it]
  (update it :sell-in dec))

(defn zero-quality [item]
  (update item :quality #(- % %)))

(defn augment-item [{:keys [name] :as item}]
  (condp = name
    AGED_BRIE      (assoc item :update-sell-in dec-sell-in  :update-quality incr-quality  :on-expire incr-quality)
    BACKSTAGE_PASS (assoc item :update-sell-in dec-sell-in  :update-quality hyper-quality :on-expire zero-quality)
    SULFURAS       (assoc item :update-sell-in identity     :update-quality identity      :on-expire identity)
    (assoc item :update-sell-in dec-sell-in  :update-quality dec-quality  :on-expire dec-quality)))

(defn process-updates [{:keys [update-sell-in update-quality] :as item}]
  (->> item
       update-sell-in
       update-quality))

(defn process-expired [{:keys [sell-in on-expire] :as item}]
  (if (neg? sell-in)
    (on-expire item)
    item))

(defn update-quality! [store]
  (doseq [item store]
    (swap! item #(->> %
                      augment-item
                      process-updates
                      process-expired))))

