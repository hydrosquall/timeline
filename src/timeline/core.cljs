(ns timeline.core
  (:require
   [rum.core :as rum]
   [timeline.utils :as utils])
  (:require-macros
   [timeline.utils :refer [inline-resource babys-first-macro]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println "--------------------------------------------------------------------")
(enable-console-print!)

; TODO: The dates "don't exist" when you first load this, because this is
; executed before the dates actually exist in the document. This then is fixed
; once you hot-reload, but that's obviously not the desired behavior.
(def inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item")))
(def years (for [d inline-date-tags] (int (.-innerText d))))

;; define your app data so that doesn't get over-written on reload
(defonce app-state (atom {:selected-date nil}))

(babys-first-macro years)

; Fetch ids
(print (for [d inline-date-tags]
         [(int (.-innerText d))
          (.-id d)]))

(defn get-percent [y -min-year -max-year]
  (* 100  (divide (- y -min-year) (- -max-year -min-year))))

; Ajust so the timeline only takes 90% of the screen
(defn get-adjusted-percent [y -min-year -max-year]
  (+ 2 (* (get-percent y -min-year -max-year) .9)))

(defn render-year [i year]
  (let [min-year (apply min years)
        max-year (apply max years)]
    [:span
     {:class "point" ; TODO: Consider using flexbox instead
      :key (str i (utils/rand-str 3))
      :style {:left (str (get-adjusted-percent year min-year max-year) "vw")}}
     year]))

(defn update-selected-date [current-app-state new-date]
  (assoc-in current-app-state [:selected-date] new-date)) ;; No need to deref it

(rum/defc hello-world < rum/reactive
  ([]
   (let [state (rum/react app-state)]
     ; Register a listener to tell this component to react to the state.
     ; - `app-state` is the reference to the atom
     ; - `state` is the value, which gets refreshed each time the atom is updated
     ;    (but is not actually the source of truth reference itself)
     [:div
      [:div {:class "timeline"} (map-indexed render-year years)]
    ; [:button {:onClick #(js/alert "hello")}  "Click me"]
      [:div {:class "spacer"}]
      [:div {:class "wrapper" :on-click (fn [e] (swap! app-state update-selected-date "foooo"))}
       [:pre "app state: " (pr-str state)]
       [:div {:class "html-text" :dangerouslySetInnerHTML {:__html example-text}}]]
      [:div {:class "spacer"}]])))

;; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))

; TODO: Decide what to render in the case where a single date occurs >1x
; (e.g. in the HOPL example)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Tests
(if (not (and
          (= 100 (get-percent 2010 2000 2010))
          (= 92 (get-adjusted-percent 2010 2000 2010)) ; = (100 * .9) + 2
          (= 11 (get-adjusted-percent 2001 2000 2010)) ; = (10 * .9) + 2
          (= 2 (get-adjusted-percent 2000 2000 2010))) ; = (0 * .9) + 2
         )
  (js/alert "Tests are failing!"))
