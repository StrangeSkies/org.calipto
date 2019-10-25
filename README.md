# Proto

Proto is a Lisp dialect with a few major twists.

Firstly, it is statically typed. Proto attempts to make advanced type system features like dependent types and higher-kinded types more accessible to the average coder, without requiring any specialist knowledge of type theory. The type system in Proto is based on a single, elegant abstraction; a type is simply a predicate which tests for membership. This turns out to be an extremely powerful foundation.

Furthermore, Proto is a purely functional language. But instead of isolating side effects monadically it isolates they by way of a strongly-typed effects system, or scoped continuations.

Lisp is widely considered to set the bar for metaprogramming facilities. Proto aspires to supplement this by providing access to the effects system during reading and compilation. This allows the programmer to establish lexical context for reading and evaluating code in which state can be maintained. In fact the type system of Proto, along with many of the compiler's optimisation capabilities, are dynamically self-hosted atop a tiny bootstrapped core.
