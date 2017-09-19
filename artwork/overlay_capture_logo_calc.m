(* Mathematica Source File  *)
(* Trig things about @drawable/overlay_ctrl_btn_capture *)
(* :Author: Luca *)
(* :Date: 2017-09-12 *)

line[z_, a_] := z*{Cos[a \[Degree]], -Sin[a \[Degree]]};
dist[x_] := Sqrt[x[[1]]^2 + x[[2]]^2] // N;

l = dist[{12, 8}]
r = dist[{12*2, l}]/2
c = {0, 0} + line[r, -30]
p1 = c + line[r/2, 90 + 60]

(* y position in screen *)
60 - l/2
