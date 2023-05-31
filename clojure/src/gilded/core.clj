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

(defn update-quality! [store]
  (doseq [item store]
    (if (and (not (= (:name @item)
                     AGED_BRIE))
             (not (= (:name @item)
                     BACKSTAGE_PASS)))

      ;; if
      (when (> (:quality @item) 0) ;; positive-quality?
        (when (not (= (:name @item) SULFURAS)) ;; if "foo"
          (swap! item update :quality #(- % 1)))) ;;decrement quality
     ;; else
      (when (< (:quality @item) 50) ;; quality less than CAP
        (swap! item update :quality #(+ % 1)) ;; raise quality
        (when (= (:name @item) BACKSTAGE_PASS) ;; if BSPass
          (when (< (:sell-in @item) 11)  ;; if with-in 11
            (when (< (:quality @item) 50) ;; keeps CAP on
              (swap! item update :quality #(+ % 1)))) ;; inc again
          (when (< (:sell-in @item) 6)  ;; then if with-in. 6
            (when (< (:quality @item) 50) ;; again if under CAP..
              (swap! item update :quality #(+ % 1))))))) ;; incr
    ;; if BSPass OR other
    ;; update date (sell-in)
    (when (not (= (:name @item) SULFURAS))
      (swap! item update :sell-in #(- % 1)))

    ;; if EXPIRED?
    (when (< (:sell-in @item) 0)
      (if (not (= (:name @item) AGED_BRIE))
        (if (not (= (:name @item) BACKSTAGE_PASS))
          ;; then
          (when (> (:quality @item) 0)                ;; pos quality? (hmmm...)
            (when (not (= (:name @item) SULFURAS))    ;; and 'OTHER'
              (swap! item update :quality #(- % 1)))) ;; quality goes down
          ;; else BSPass ... EXPIRED-TICKET!! zappo!!
          (swap! item update :quality #(- % %)))
        ;; ELSE its AGED_BRIE quality goes up UNDER CAP
        (when (< (:quality @item) 50)
          (swap! item update :quality #(+ % 1)))))))
