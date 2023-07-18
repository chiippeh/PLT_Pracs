// Sieve of Eratosthenes using IntSet abstract data type
// *names*, Rhodes University, 2023

import library.*;

class sieveSet {

  static public void main(String[] args) {
    final int Max = 49000;
    IntSet mySet = new IntSet();
    int i, n, k, it, iterations, primes = 0;
    { IO.write("How many iterations? "); iterations = IO.readInt(); }
    { IO.write("Supply largest number to be tested "); n = IO.readInt(); }
    if (n > Max) {
      { IO.write("n too large, sorry"); }
      return;
    }
    it = 1;
    while (it <= iterations) {
      primes = 0;
      i = 2;
      while (i <= n) {
        if (!mySet.contains(i)) { // if mySet doesn't contain i - must be a prime
          primes = primes + 1;
          k = i;
          mySet.incl(k); // add i to mySet
          k = k + i;
          while (k <= n) { // mark all multiples of i as not prime
            mySet.incl(k);
            k = k + i;
          }      
        }
        i = i + 1; // next number
      }
      it = it + 1; // next iteration
    }
    { IO.write(primes); IO.write(" primes"); }
  } // main
  
} // sieveSet