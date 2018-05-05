include Deferred

let rec fib n =
    (match n with
        | 0 -> Deferred.return 1
        | 1 -> Deferred.return 1
        | n -> fib(n-1) + fib(n-2)
    );;

