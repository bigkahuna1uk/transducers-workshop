(ns transducers-workshop.solutions.lab01
  (:require [clojure.edn :as edn]
            [transducers-workshop.xf :as xf])
  (:import java.util.Date))

; data shape
; [{:fee-attributes []
;   :product {}
;   :created-at 111111}
;  {:fee-attributes []
;   :product {}
;   :created-at 111111}]

(defn load-data
  "Load example feed from disk."
  []
  (edn/read-string (slurp "feed.edn")))

(def prepare-data
  (comp
    (xf/merge-into :product [:fee-attribute :created-at])
    (xf/update-at :created-at #(Date. %))))

(defn filter-data [params]
  (comp
    (xf/allow-if :visible)
    (xf/allow-if :online)
    (xf/allow-if-equal :company-id (params :company-id))
    (xf/allow-if (params :repayment-method))
    (xf/allow-in-range :min-loan-amount :max-loan-amount (params :loan-amount))
    ))

(defn xform [params]
  (comp
    prepare-data
    (filter-data params)
    ))

(defn products [params feed]
  (sequence (xform params) feed))

;; Task number 1: prepare and filter the data.
;; How to use at the REPL
; (require '[transducers-workshop.lab01 :as lab01] :reload)

; (def xs
;   (lab01/products {; :company-id 46
;                    :repayment-method :payment-method-part-repayment
;                    :loan-amount 1500000}
;                   (lab01/load-data)))
; (count xs)
; 69

(defn create-search [params]
  (eduction (xform params) (load-data)))

(def company1 (create-search {:company-id 46}))
(def company2 (create-search {:company-id 50}))

;; Task 2: create an eduction over some frequently used company filters.
;; What is eduction allowing in this scenario? Here's an example of what you should see:

; (map :name lab01/company1)

; ("Green Professional Credit Intermediary 1.5% Buyer AA123 3 0.75% Legals"
;  "AAA132 IO A130 BBB124 AAA125 Older Self / only) Year"
;  "(DMS) Clear128 Fixed Intrinsic Part/Part Switcher AA130 BiR Tracker Reward")

; (map :name lab01/company2)

; ("Loan Monthly AAA124 C/A Product A126 1% Tier 10 Starter")
