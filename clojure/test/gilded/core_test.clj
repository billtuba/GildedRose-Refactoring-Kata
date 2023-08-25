(ns gilded.core-test
  (:require [clojure.test :as t]
            [gilded.core :as sut]
            [matcher-combinators.standalone :as m]))

(def BACKSTAGE_PASS @#'sut/BACKSTAGE_PASS)
(def SULFURAS @#'sut/SULFURAS)
(def AGED_BRIE @#'sut/AGED_BRIE)

(t/deftest backstage-passes-increase-with-age-up-to-expiration
  (t/are [it expected]
         (m/match? expected (#'sut/update-item it))

    {:name BACKSTAGE_PASS :quality 1 :sell-in 11}
    {:name BACKSTAGE_PASS :quality 2 :sell-in 10}

    {:name BACKSTAGE_PASS :quality 1 :sell-in 10}
    {:name BACKSTAGE_PASS :quality 3 :sell-in 9}

    {:name BACKSTAGE_PASS :quality 1 :sell-in 5}
    {:name BACKSTAGE_PASS :quality 4 :sell-in 4}

    {:name BACKSTAGE_PASS :quality 1 :sell-in 1}
    {:name BACKSTAGE_PASS :quality 4 :sell-in 0}

    {:name BACKSTAGE_PASS :quality 50 :sell-in 0}
    {:name BACKSTAGE_PASS :quality 0  :sell-in -1}))

(t/deftest sulfuras-never-decreases-in-quality
  (t/are [it expected]
         (m/match? expected (#'sut/update-item it))

    {:name SULFURAS :quality 10 :sell-in 1}
    {:name SULFURAS :quality 10 :sell-in 1}

    {:name SULFURAS :quality 10 :sell-in 0}
    {:name SULFURAS :quality 10 :sell-in 0}))

(t/deftest quality-is-capped-at-50
  (t/are [it expected]
         (m/match? expected       (#'sut/update-item it))

    {:name AGED_BRIE :quality 49 :sell-in 2}
    {:name AGED_BRIE :quality 50 :sell-in 1}

    {:name AGED_BRIE :quality 50 :sell-in 2}
    {:name AGED_BRIE :quality 50 :sell-in 1}))

(t/deftest aged-brie-quality-increases-with-age
  (t/are [it expected]
         (m/match? expected (#'sut/update-item it))
    {:name AGED_BRIE :quality 0 :sell-in 2}
    {:name AGED_BRIE :quality 1 :sell-in 1}

    {:name AGED_BRIE :quality 1 :sell-in 1}
    {:name AGED_BRIE :quality 2 :sell-in 0}

    {:name AGED_BRIE :quality 2 :sell-in 0}
    {:name AGED_BRIE :quality 4 :sell-in -1}

    {:name AGED_BRIE :quality 4 :sell-in -1}
    {:name AGED_BRIE :quality 6 :sell-in -2}

    {:name AGED_BRIE :quality 6 :sell-in -2}
    {:name AGED_BRIE :quality 8 :sell-in -3}))

(t/deftest quality-degrades-2x-past-expiry-but-never-neg
  (t/are [it expected]
         (m/match? expected (#'sut/update-item it))
    {:name "foo" :quality 1 :sell-in  1}
    {:name "foo" :quality 0 :sell-in  0}

    {:name "foo" :quality 0 :sell-in -1}
    {:name "foo" :quality 0 :sell-in -2}))

(t/deftest conjured-degradation-test
  (t/testing "'Conjured' items degrade in Quality twice as fast as normal items"
    (t/are [it expected]
           (m/match? expected (#'sut/update-item it))
      {:name "Conjured" :quality 50 :sell-in 5}
      {:name "Conjured" :quality 25 :sell-in 4}

      {:name "Conjured" :quality 25 :sell-in 4}
      {:name "Conjured" :quality 12 :sell-in 3}

      {:name "Conjured" :quality 12 :sell-in 3}
      {:name "Conjured" :quality  6 :sell-in 2}

      {:name "Conjured" :quality  6 :sell-in 2}
      {:name "Conjured" :quality  3 :sell-in 1}

      {:name "Conjured" :quality  3 :sell-in 1}
      {:name "Conjured" :quality  1 :sell-in 0}

      {:name "Conjured" :quality  1 :sell-in  0}
      {:name "Conjured" :quality  0 :sell-in -1}

      {:name "Conjured" :quality  0 :sell-in -1}
      {:name "Conjured" :quality  0 :sell-in -2})))
