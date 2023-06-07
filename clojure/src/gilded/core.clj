(ns gilded.core)

(def AGED_BRIE "Aged Brie")
(def BACKSTAGE_PASS "Backstage passes to a TAFKAL80ETC concert")
(def SULFURAS "Sulfuras, Hand of Ragnaros")


(defn make-store [items]
  (->> items
       (map (fn [item] (atom item)))))

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
    AGED_BRIE      (assoc item :update-sell-in dec-sell-in  :update-quality incr-quality  :update-expired incr-quality)
    BACKSTAGE_PASS (assoc item :update-sell-in dec-sell-in  :update-quality hyper-quality :update-expired zero-quality)
    SULFURAS       (assoc item :update-sell-in identity     :update-quality identity      :update-expired identity)
    (assoc item :update-sell-in dec-sell-in  :update-quality dec-quality  :update-expired dec-quality)))

(defn process-updates [{:keys [update-sell-in update-quality] :as item}]
  (->> item
       update-sell-in
       update-quality))

(defn process-expired [{:keys [sell-in update-expired] :as item}]
  (if (neg? sell-in)
    (update-expired item)
    item))

(defn update-item [item]
  (->> item
       augment-item
       process-updates
       process-expired))

(defn update-items! [store]
  (doseq [item store]
    (swap! item update-item)))

