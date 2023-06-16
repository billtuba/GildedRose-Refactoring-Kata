(ns gilded.core)

(defn- incr-quality [item]
  (if (< (:quality item) 50)
    (update item :quality inc)
    item))

(defn- incr-quality-2x [{:keys [sell-in] :as  item}]
  (cond->>
   (incr-quality item)
    (< sell-in 10) incr-quality
    (< sell-in 5)  incr-quality))

(defn- quality-degrades [item]
  (if (> (:quality item) 0)
    (update item :quality dec)
    item))

(defn- quality-degrades-2x [item]
  (update item :quality #(int (/ % 2))))

(defn- dec-sell-in [it]
  (update it :sell-in dec))

(defn- zero-quality [item]
  (update item :quality #(- % %)))

(defmulti  update-sell-in :type)
(defmethod update-sell-in :default   [item]      (dec-sell-in item))
(defmethod update-sell-in :aged-brie [item]      (dec-sell-in item))
(defmethod update-sell-in :conjured  [item]      (dec-sell-in item))
(defmethod update-sell-in :backstage-pass [item] (dec-sell-in item))
(defmethod update-sell-in :sulfuras  [item]      (identity item))

(defmulti  update-quality :type)
(defmethod update-quality :default   [item]      (quality-degrades item))
(defmethod update-quality :aged-brie [item]      (incr-quality item))
(defmethod update-quality :backstage-pass [item] (incr-quality-2x item))
(defmethod update-quality :conjured  [item]      (quality-degrades-2x item))
(defmethod update-quality :sulfuras  [item]      (identity item))

(defmulti  update-expired :type)
(defmethod update-expired :default   [item]      (quality-degrades item))
(defmethod update-expired :aged-brie [item]      (incr-quality item))
(defmethod update-expired :conjured  [item]      (identity item))
(defmethod update-expired :backstage-pass [item] (zero-quality item))
(defmethod update-expired :sulfuras  [item]      (identity item))

(defn- process-expired [{:keys [sell-in] :as item}]
  (if (neg? sell-in)
    (update-expired item)
    item))

(def ^:private AGED_BRIE      "Aged Brie")
(def ^:private CONJURED       "Conjured")
(def ^:private BACKSTAGE_PASS "Backstage passes to a TAFKAL80ETC concert")
(def ^:private SULFURAS       "Sulfuras, Hand of Ragnaros")

(def ^:private name->type
  {AGED_BRIE      :aged-brie
   BACKSTAGE_PASS :backstage-pass
   SULFURAS       :sulfuras
   CONJURED       :conjured})

(defn- append-type
  [{:keys [name] :as it}]
  (assoc it :type (get name->type name)))

(defn- update-item [item]
  (->> item
       append-type
       update-sell-in
       update-quality
       process-expired))

(defn- trim-output [item]
  (select-keys item [:name :quality :sell-in]))

(def update-items (partial map (comp trim-output update-item)))

