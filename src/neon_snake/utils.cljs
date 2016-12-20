(ns neon-snake.utils
  (:require [cljs.pprint :refer [pprint]]))

; load sounds

(defonce robo-blip (js/document.getElementById "robo-blip"))
(defonce background-song (js/document.getElementById "background-song"))
(defonce game-over-sound (js/document.getElementById "game-over"))

(defn play-blip-sound []
  (.play robo-blip))

(defn play-background-song []
  (.play background-song))

(defn pause-background-song []
  (.pause background-song))

(defn play-game-over-sound []
  (.play game-over-sound))


; food item position is stored in :point
; wee need to make sure that new food position does not collide with
; the snake body
; We write a function that returns a random free position
; if there is no space left, the function will return nil

(defn rand-free-position
  "This function takes the snake and the board-size as arguments,
   and returns a random position not colliding with the snake body"
  [snake [x y]]
  (let [snake-position-set (into #{} (:body snake))
        board-positions (for [x-pos (range x)
                              y-pos (range y)]
                          [x-pos y-pos])]
        (when-let [free-positions (seq (remove snake-position-set board-positions))]
          (rand-nth free-positions))))


(defn move-snake
  "Move the snake based on positions nd directions of each snake body segments"
  [{:keys [direction body] :as snake}]
  (let [head-new-position (mapv + direction (first body))]
    (update-in snake [:body] #(into [] (drop-last (cons head-new-position body))))))

(defonce up 38)
(defonce down 40)
(defonce left 37)
(defonce right 39)

; a map of key code to direction vector
(def key-code->move
  {up [0 -1] down [0 1] left [-1 0] right  [1 0]})

; ignore movements in the oposite direction
(defn change-snake-direction
  "Change the snake head direction, only when it is perpendicular to old head direction"
  [[new-x new-y] [old-x old-y]]
  (if (or
        (= new-x old-x)
        (= new-y old-y))
      [old-x old-y]
      [new-x new-y]))

(defn snake-tail [[[x1 y1] [x2 y2 :as last-piece]]]
  "Computes a new tail piece (coord vector) based on last two pieces of the snake"
  (let [diff-vec [(- x2 x1) (- y2 y1)]]
    (mapv + diff-vec last-piece)))


(defn grow-snake
  "Append a new segment to the tail of the snake"
  [{:keys [body] :as snake}]
  (let [last-two-pieces (take-last 2 body)
        new-piece (snake-tail last-two-pieces)]
    (update-in snake [:body] #(conj % new-piece))))

(defn process-move
  "Evaluate the new snake position in the context of the whole game "
  [{:keys [snake point board] :as db}]
  (if (= point (first (:body snake)))
    (do
      (play-blip-sound)
      (-> db
          (update-in [:snake] grow-snake)
          (update-in [:points] inc)
          (assoc :point (rand-free-position snake board))))
    db))

(defn collision?
  "Returns truee if snake collides with borders of board or with itself"
  [snake board]
  (let [{:keys [body direction]} snake
        [x y] board
        border-x #{x -1}
        border-y #{y -1}
        next-x (+ (first direction) (ffirst body))
        next-y (+ (second direction) (second (first body)))
        tail-pieces (rest body)]

    (or
      (contains? border-x next-x)
      (contains? border-y next-y)
      (contains? (into #{} tail-pieces) [next-x next-y]))))

