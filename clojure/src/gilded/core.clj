(ns gilded.core)

(def AGED_BRIE      "Aged Brie")
(def CONJURED       "Conjured")
(def BACKSTAGE_PASS "Backstage passes to a TAFKAL80ETC concert")
(def SULFURAS       "Sulfuras, Hand of Ragnaros")

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

(defn quality-degrades [item]
  (if (> (:quality item) 0)
    (update item :quality dec)
    item))

(defn quality-degrades-2x [item]
  (update item :quality #(int (/ % 2))))

(defn dec-sell-in [it]
  (update it :sell-in dec))

(defn zero-quality [item]
  (update item :quality #(- % %)))

(defn augment-item [{:keys [name] :as item}]
  (condp = name
    AGED_BRIE      (assoc item :update-sell-in dec-sell-in  :update-quality incr-quality         :update-expired incr-quality)
    BACKSTAGE_PASS (assoc item :update-sell-in dec-sell-in  :update-quality hyper-quality        :update-expired zero-quality)
    SULFURAS       (assoc item :update-sell-in identity     :update-quality identity             :update-expired identity)
    CONJURED       (assoc item :update-sell-in dec-sell-in  :update-quality quality-degrades-2x  :update-expired identity)
    (assoc item :update-sell-in dec-sell-in  :update-quality quality-degrades  :update-expired quality-degrades)))

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

(defn trim-output [item]
  (select-keys item [:name :quality :sell-in]) )

(defn update-items [store]
  (iterate #(map (comp trim-output update-item) %) store))

