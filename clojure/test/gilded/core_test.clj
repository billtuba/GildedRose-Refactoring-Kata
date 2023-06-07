(ns gilded.core-test
  (:require [clojure.test :as t]
            [gilded.core :as sut]
            [matcher-combinators.test :refer [match?]]))

(t/deftest backstage-passes-increase-with-age-up-to-expiration
  (let [fixture  [{:name sut/BACKSTAGE_PASS :quality 1 :sell-in 12}]
        store (sut/make-store fixture)]
    (t/are [expected]
           (do
             (sut/update-quality! store)
             (t/is (match? expected (first (sut/item-seq store)))))
      {:name sut/BACKSTAGE_PASS :quality  2 :sell-in 11}
      {:name sut/BACKSTAGE_PASS :quality  3 :sell-in 10}
      {:name sut/BACKSTAGE_PASS :quality  5 :sell-in  9}
      {:name sut/BACKSTAGE_PASS :quality  7 :sell-in  8}
      {:name sut/BACKSTAGE_PASS :quality  9 :sell-in  7}
      {:name sut/BACKSTAGE_PASS :quality 11 :sell-in  6}
      {:name sut/BACKSTAGE_PASS :quality 13 :sell-in  5}
      {:name sut/BACKSTAGE_PASS :quality 16 :sell-in  4}
      {:name sut/BACKSTAGE_PASS :quality 19 :sell-in  3}
      {:name sut/BACKSTAGE_PASS :quality 22 :sell-in  2}
      {:name sut/BACKSTAGE_PASS :quality 25 :sell-in  1}
      {:name sut/BACKSTAGE_PASS :quality 28 :sell-in  0}
      {:name sut/BACKSTAGE_PASS :quality  0 :sell-in -1})))

(t/deftest sulfuras-never-decreases-in-quality
  (let [fixture  [{:name sut/SULFURAS :quality 10 :sell-in 1}]
        store (sut/make-store fixture)]
    (t/are [expected]
           (do
             (sut/update-quality! store)
             (t/is (match? expected (first (sut/item-seq store)))))
      {:name sut/SULFURAS :quality 10 :sell-in 1}
      {:name sut/SULFURAS :quality 10 :sell-in 1})))

(t/deftest quality-is-capped-at-50
  (let [fixture  [{:name sut/AGED_BRIE :quality 49 :sell-in 2}]
        store (sut/make-store fixture)]
    (t/are [expected]
           (do
             (sut/update-quality! store)
             (t/is (match? expected (first (sut/item-seq store)))))
      {:name sut/AGED_BRIE :quality 50 :sell-in 1}
      {:name sut/AGED_BRIE :quality 50 :sell-in 0})))

(t/deftest aged-brie-quality-increases-with-age
  (let [fixture  [{:name sut/AGED_BRIE  :quality 0 :sell-in 2}]
        store (sut/make-store fixture)]
    (t/are [expected]
           (do
             (sut/update-quality! store)
             (t/is (match? expected (first (sut/item-seq store)))))
      {:name sut/AGED_BRIE :quality 1 :sell-in  1}
      {:name sut/AGED_BRIE :quality 2 :sell-in  0}
      {:name sut/AGED_BRIE :quality 4 :sell-in -1}
      {:name sut/AGED_BRIE :quality 6 :sell-in -2}
      {:name sut/AGED_BRIE :quality 8 :sell-in -3})))

(t/deftest quality-degrades-2x-past-expiry-but-never-neg
  (let [fixture  [{:name "foo"  :quality 10 :sell-in 2}]
        store (sut/make-store fixture)]
    (t/are [expected]
           (do (sut/update-quality! store)
               (t/is (match? expected (first (sut/item-seq store)))))
      {:name "foo" :quality 9 :sell-in  1}
      {:name "foo" :quality 8 :sell-in  0}
      {:name "foo" :quality 6 :sell-in -1}
      {:name "foo" :quality 4 :sell-in -2}
      {:name "foo" :quality 2 :sell-in -3}
      {:name "foo" :quality 0 :sell-in -4}
      {:name "foo" :quality 0 :sell-in -5})))
