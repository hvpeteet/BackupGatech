print_string("==========\n");;

let rec kth_elem l i =
    (match l with
        | [] -> None
        | h :: t -> if i = 0 then Some h else kth_elem t (i-1)
    );;

kth_elem [1;2;3;4] 0;;

let rec length l =
    (match l with
        | [] -> 0
        | _ :: t -> 1 + length t
    );;

length [1;2;3;4;5;6;1;2;3;4;5;6];;

let rec reverse l =
    let rec move lold lnew =
        (match lold with
            | [] -> lnew
            | h::t -> move t (h::lnew)
        ) in
        move l [];;

reverse [1;2;3;4;5];;

let rec pal l =
    reverse l = l;;

pal [1;2;3;2;3];;