(ns acme.frontend.app
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [reagent.core :as r]
            [reagent.dom :as rd]))


(def units {"cup" {:unit/type :unit.type/volume
                   :unit/name "cup"
                   :unit/factor 236.588}
            "tablespoon" {:unit/type :unit.type/volume
                          :unit/name "tablespoon"
                          :unit/factor 14.7868}
            "teaspoon" {:unit/type :unit.type/volume
                        :unit/name "teaspoon"
                        :unit/factor 4.92892}
            "oz" {:unit/type :unit.type/mass
                  :unit/name "oz"
                  :unit/factor 28.3495}
            "pound" {:unit/type :unit.type/mass
                     :unit/name "pound"
                     :unit/factor 453.592}
            "gram" {:unit/type :unit.type/mass
                    :unit/name "gram"
                    :unit/factor 1}
            "mL" {:unit/type :unit.type/volume
                  :unit/name "mL"
                  :unit/factor 1}
            "count" {:unit/type :unit.type/quantity
                     :unit/name "count"
                     :unit/factor 1}})

; input numbers are integers, decimals, or fractions
; Examples: "1" "1.5" "3/4" "1 2/3"

(spec/def :db/id any?)

(def integer-pattern #"\d+")
(def fraction-pattern #"(\d+ )?\d+/\d+")
(def decimal-pattern #"\d+\.\d+")

(defn matches?
  [pattern]
  (fn [s]
    (some? (re-matches pattern s))))

(spec/def :input/number (spec/and string?
                                  (spec/or :fraction (matches? fraction-pattern)
                                           :integer (matches? integer-pattern)
                                           :decimal (matches? decimal-pattern))))

(spec/def :input/name (spec/and string? not-empty))

(spec/def :input/measurement
  (spec/tuple :input/number :unit/name))

(spec/def :unit/unit
  (spec/keys :req [:unit/type :unit/name :unit/factor]))


(spec/def :unit/name #(contains? units %))
(spec/def :unit/factor pos?)
(spec/def :unit/type #{:unit.type/volume :unit.type/mass :unit.type/quantity})

(spec/def :ingredient/input
  (spec/keys :req [:ingredient/name :ingredient/calorie-density]))

(spec/def :ingredient/db
  (spec/keys :req [:ingredient/name :ingredient/calorie-density]))

(spec/def :ingredient/calorie-density
  (spec/map-of :unit/type (spec/keys :req [:calorie-density/measurement
                                           :calorie-density/calories])))

(spec/def :calorie-density/measurement :input/measurement)
(spec/def :calorie-density/calories integer?)


(spec/def :ingredient/id :db/id)
(spec/def :ingredient/name :input/name)


(spec/def :recipe/input
  (spec/keys :req [:recipe/name
                   :recipe/ingredients
                   :recipe/totals]
             :opt [:recipe/notes]))

(spec/def :recipe/name :input/name)
(spec/def :recipe/notes string?)
(spec/def :recipe/ingredients (spec/coll-of :recipe/ingredient))
(spec/def :recipe/ingredient
  (spec/keys :req [:input/measurement
                   :ingredient/id]))

(spec/def :recipe/totals
  (spec/map-of :unit/type :input/measurement))


(defn read-fraction
  [s]
  (let [[x y] (string/split s #" ")]
    (if (some? y)
      (+ (js/parseInt x) (read-fraction y))
      (let [[x y] (string/split s #"/")]
        (/ (js/parseInt x) (js/parseInt y))))))


(defn read-input-number
  [s]
  (if-let [[kind v] (spec/conform :input/number s)]
    (cond (= kind :fraction) (read-fraction v)    
          (= kind :decimal) (js/parseFloat v) 
          (= kind :integer) (js/parseInt v 10))))

(defn output
  [msg]
  (-> js/document
      (.getElementById "root")
      (.-innerHTML)
      (set! (str msg))))


(defn init 
  []
  (output "hello!"))


(def test-ingredients
  {0 {:ingredient/name "carbs"
      :ingredient/calorie-density 
      {:unit.type/volume {:calorie-density/measurement ["1 1/2" "cup"]
                          :calorie-density/calories 500}}}
   1 {:ingredient/name "fat sticks"
      :ingredient/calorie-density
      {:unit.type/volume {:calorie-density/measurement ["1 1/2" "cup"]
                          :calorie-density/calories 500}}}
   2 {:ingredient/name "eggs"
      :ingredient/calorie-density
      {:unit.type/quantity {:calorie-density/measurement ["1" "count"]
                            :calorie-density/calories 50}}}
   3 {:ingredient/name "heavy fat"
      :ingredient/calorie-density
      {:unit.type/mass {:calorie-density/measurement ["50" "gram"]
                        :calorie-density/calories 150}}}})

; manual calulation
; ingredient calorie density
; 0 carbs
; 500 calories / 1.5 cups
; 500 calories / 354.882 mL
; 1.408919020970351 calories / mL
;
; 1 fat sticks
; 500 calories / 1.5 cups
; 1.408919020970351 calories / mL
;
; 2 eggs
; 50 calories / 1 count
;
; 4 heavy fat
; 150 calories / 50 gram
; 3 calories / gram
;
; recipe amounts
; 1/2 cup of carbs = 118.294 mL of carbs = 166.666666666666701 calories
; 1.5 tablespoons of fat sticks = 22.1802 mL of fat stick = 31.250105668926579 calories 
; 3 count of eggs = 150 calories
; 8 oz of heavy fat = 226.796 g of heavy fat = 680.388 calories 
; total calories = 1029.20477233593
; from code = 1028.304772335593 (close enough, remember how I can't type?) 
; total amount recipe makes = 1000 grams
; calorie density of recipe = 1028.3 caloires / 1000 grams = 1.028 calories / gram
;
; how many calories are 8 oz of the receipe
; 8 oz = 226.796 grams 
;
; 1.028 calories / gram * 226.796 grams = 233 calories
; from code = 233 calories
;
; boom, verified! (well at least for this case)
;


(def recipe0
  {:recipe/name "test recipe 0"
   :recipe/ingredients [{:input/measurement ["1/2" "cup"]
                         :ingredient/id 0}
                        {:input/measurement ["1.5" "tablespoon"]
                         :ingredient/id 1}
                        {:input/measurement ["3" "count"]
                         :ingredient/id 2}
                        {:input/measurement ["8" "oz"]
                         :ingredient/id 3}]
   :recipe/totals {:unit.type/mass ["1000" "gram"]}})


(defn sub-ingredients
  [recipe ingredients]
  (update recipe 
          :recipe/ingredients
          (fn [is]
            (map (fn [{:keys [ingredient/id] :as ri}]
                   (assoc ri
                          :ingredient
                          (get ingredients id)))
                 is))))


(defn calc-ingredient-density
  [ingredient unit-type]
  (let [density (get-in ingredient [:ingredient/calorie-density unit-type])
        [m u] (:calorie-density/measurement density)
        cal (:calorie-density/calories density)
        measure (read-input-number m)
        {factor :unit/factor} (get units u)]
    (/ cal (* measure factor))))


(defn calories-in
  "Calculates the total calories for the recipe"
  [{:keys [recipe/ingredients] :as recipe}]
  (reduce (fn [total {[m u] :input/measurement in :ingredient}]
            (let [amount (read-input-number m)
                  unit (get units u)
                  density (calc-ingredient-density in (:unit/type unit))
                  base-amount (* amount (:unit/factor unit))]
              (+ total (* density base-amount))))
          0
          ingredients))

(defn how-much
  [recipe amount unit-name]
  (let [total-calories (calories-in (sub-ingredients recipe test-ingredients))
        amount-unit (get units unit-name)
        [total-amount total-unit] (get-in recipe [:recipe/totals (:unit/type amount-unit)])
        total-amount-in-base (* (read-input-number total-amount)
                                (get-in units [total-unit :unit/factor]))
        amount-in-base (* (read-input-number amount)
                          (:unit/factor amount-unit))]
    (* amount-in-base (/ total-calories total-amount-in-base))))


(println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
(prn "calories-in" (calories-in (sub-ingredients recipe0 test-ingredients)))
(prn "how-much" (how-much recipe0 "8" "oz"))
(println "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")


(defn on-fauna-click
  []
  (js/console.log "fauna click")
  (if-let [user (js/netlifyIdentity.currentUser)]
    (-> (.jwt user)
        (.then (fn [jwt]
                 (js/fetch
                   ".netlify/functions/faunakey"
                   #js {:headers #js {"Content-Type" "application/json"
                                      "Authorization" (str "Bearer " jwt)}})))
        (.catch (fn [err]
                  (js/console.error err))))
    
    (js/console.log "not logged in")))


(let [el (js/document.getElementById "fauna-btn")]
  (.addEventListener el "click" on-fauna-click))


(defn app
  []
  [:p "rendered with react"])


(rd/render [app]
           (js/document.getElementById "app"))
