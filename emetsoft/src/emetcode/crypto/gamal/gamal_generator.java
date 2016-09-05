package emetcode.crypto.gamal;

/*
 * Makes a random safe prime p of a given minimum bit length,
 * and a generator mod p.
 * [Fermat little theorem => g^(p-1) = 1 mod p if p prime and not p|g;
 * A generator mod p is an integer g such that for all 0 < k < p-1, g^k <> 1
 * mod p. It follows that the g^k % p generate all the integers 0 < n < p.]
 */

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

import emetcode.util.devel.logger;

public class gamal_generator {

	private static final boolean DEBUG = false;

	public static final BigInteger ZERO = BigInteger.ZERO,
			ONE = BigInteger.ONE, TWO = ONE.add(ONE), THREE = TWO.add(ONE);

	private BigInteger prime_p, alpha;

	// Constructors
	public gamal_generator(int num_bits, int certainty, SecureRandom rnd) {
		if (DEBUG) {
			if (num_bits < 512) {
				logger.info("WARNING: Safe primes should be >= 512 bits long");
			}
			logger.debug("Making a safe prime of at least %d bits...\n",
					num_bits);
		}

		long startTm = System.currentTimeMillis(), endTm;
		generate_primes(num_bits, certainty, rnd);
		endTm = System.currentTimeMillis();

		if (DEBUG) {
			logger.debug("Generating p, g took %d ms\n", endTm - startTm);
			logger.debug("p = %x (%d bits)\n", prime_p, prime_p.bitLength());
			logger.debug("g = %x (%d bits)\n", alpha, alpha.bitLength());
		}
	}

	public gamal_generator(String pub) {
		try {
			int idx_p = pub.indexOf('p');
			if (idx_p == -1) {
				throw new bad_gamal(2);
			}
			int idx_g = pub.indexOf('g');
			if (idx_g == -1) {
				throw new bad_gamal(2);
			}

			if (idx_p >= idx_g) {
				throw new bad_gamal(2);
			}

			String str_p = pub.substring(0, idx_p);
			String str_alpha = pub.substring(idx_p + 1, idx_g);

			prime_p = new BigInteger(str_p);
			alpha = new BigInteger(str_alpha);

		} catch (NumberFormatException ex) {
			throw new bad_gamal(2, ex.toString());
		} catch (IndexOutOfBoundsException ex) {
			throw new bad_gamal(2, ex.toString());
		}
	}

	public String get_public_string() {
		String pub = prime_p + "p" + alpha + "g";
		return pub;
	}

	/*
	 * Method to make a safe prime (stored in this.p) and a generator (this.g)
	 * mod p. Uses method suggested by D Bishop: (1) Find a safe prime p = 2rt +
	 * 1 where r is smallish (~10^9), t prime; (2) Obtain the prime
	 * factorization of p-1 (quickish in view of (1)); (3) Repeatedly make a
	 * random g, 1 < g < p until for each prime factor f of p-1, g^((p-1)/f) is
	 * incogruent to 1 mod p.
	 */
	public void generate_primes(int num_bits, int certainty, SecureRandom rnd) {
		BigInteger r = BigInteger.valueOf(0x7fffffff), t = new BigInteger(
				num_bits, certainty, rnd);

		// (1) make prime p
		do {
			r = r.add(ONE);
			prime_p = TWO.multiply(r).multiply(t).add(ONE);
		} while (!prime_p.isProbablePrime(certainty));

		// (2) obtain prime factorization of p-1 = 2rt
		HashSet<BigInteger> factors = new HashSet<BigInteger>();
		factors.add(t);
		factors.add(TWO);
		if (r.isProbablePrime(certainty)) {
			factors.add(r);
		} else {
			factors.addAll(find_factors(r));
		}

		// We have set of prime factors of p-1.
		// Now (3) look for a generator mod p. Repeatedly make
		// a random g, 1 < g < p, until for each prime factor f of p-1,
		// g^((p-1)/f) is incogruent to 1 mod p.

		BigInteger pMinusOne = prime_p.subtract(ONE), z, lnr;
		boolean isGen;
		do {
			isGen = true;
			alpha = new BigInteger(prime_p.bitLength() - 1, rnd); // random, < p
			for (BigInteger f : factors) { // check cond on g for each f
				z = pMinusOne.divide(f);
				lnr = alpha.modPow(z, prime_p);
				if (lnr.equals(ONE)) {
					isGen = false;
					break;
				}
			}
		} while (!isGen);

		// Now g is a generator mod p
	} // end of makeSafePrimeAndGenerator() method

	public static HashSet<BigInteger> find_factors(BigInteger n) {
		BigInteger nn = new BigInteger(n.toByteArray()); // clone n
		HashSet<BigInteger> factors = new HashSet<BigInteger>();
		BigInteger dvsr = TWO, dvsrSq = dvsr.multiply(dvsr);

		while (dvsrSq.compareTo(nn) <= 0) { // divisor <= sqrt of n
			if (nn.mod(dvsr).equals(ZERO)) { // found a factor (must be prime):
				factors.add(dvsr); // add it to set
				while (nn.mod(dvsr).equals(ZERO)) { // divide it out from n
													// completely
					nn = nn.divide(dvsr); // (ensures later factors are prime)
				}

			}
			dvsr = dvsr.add(ONE); // next possible divisor
			dvsrSq = dvsr.multiply(dvsr);
		}

		// if nn's largest prime factor had multiplicity >= 2, nn will now be 1;
		// if the multimplicity is only 1, the loop will have been exited
		// leaving
		// nn == this prime factor;
		if (nn.compareTo(ONE) > 0) {
			factors.add(nn);
		}
		return factors;
	}

	public BigInteger get_p() {
		return prime_p;
	}

	public BigInteger get_g() {
		return alpha;
	}

}
