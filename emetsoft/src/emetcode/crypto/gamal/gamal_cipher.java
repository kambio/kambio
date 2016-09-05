package emetcode.crypto.gamal;

import java.math.BigInteger;
import java.security.SecureRandom;

import emetcode.util.devel.logger;

public class gamal_cipher {

	// p is p
	// g is alpha
	// a is a
	// r is beta

	public BigInteger secret_a, prime_p, alpha, beta, pMinus2;

	private SecureRandom random_gen;

	public static final BigInteger ZERO = BigInteger.ZERO,
			ONE = BigInteger.ONE, TWO = ONE.add(ONE), THREE = TWO.add(ONE);

	// Two constructors //Option 1: generate a random system using specified key
	// size; save config:
	public gamal_cipher(gamal_generator fact, SecureRandom rnd) {
		// Random system with at least num_bits bits in p
		random_gen = null; // prevents encrypt use

		prime_p = fact.get_p();
		alpha = fact.get_g();
		pMinus2 = prime_p.subtract(TWO);

		// a should be a random integer in range 1 < a < p-1
		BigInteger pmt = prime_p.subtract(THREE);
		secret_a = (new BigInteger(prime_p.bitLength(), rnd)).mod(pmt).add(TWO);
		beta = alpha.modPow(secret_a, prime_p);
	}

	public int max_msg_bytes() {
		return (prime_p.bitLength() / 8);
	}

	public gamal_cipher(String pub, SecureRandom rnd) {
		random_gen = rnd;
		secret_a = ZERO; // prevents decrypt use
		try {
			int idx_p = pub.indexOf('p');
			if (idx_p == -1) {
				throw new bad_gamal(2);
			}
			int idx_g = pub.indexOf('g');
			if (idx_g == -1) {
				throw new bad_gamal(2);
			}
			int idx_r = pub.indexOf('r');
			if (idx_r == -1) {
				throw new bad_gamal(2);
			}

			if (idx_p >= idx_g) {
				throw new bad_gamal(2);
			}
			if (idx_g >= idx_r) {
				throw new bad_gamal(2);
			}

			String str_p = pub.substring(0, idx_p);
			String str_alpha = pub.substring(idx_p + 1, idx_g);
			String str_beta = pub.substring(idx_g + 1, idx_r);

			prime_p = new BigInteger(str_p);
			alpha = new BigInteger(str_alpha);
			beta = new BigInteger(str_beta);

			pMinus2 = prime_p.subtract(TWO);

		} catch (NumberFormatException ex) {
			throw new bad_gamal(2, ex.toString());
		} catch (IndexOutOfBoundsException ex) {
			throw new bad_gamal(2, ex.toString());
		}
	}

	public String get_public_string() {
		String pub = prime_p + "p" + alpha + "g" + beta + "r";
		return pub;
	}

	// A message block is considered a BigInteger.
	// Returns pair of BigIntegers comprising the ElGamal cipher-"text"
	public BigInteger[] encrypt_base(BigInteger m) {
		if (random_gen == null) {
			throw new bad_gamal(2);
		}

		BigInteger k = new BigInteger(prime_p.bitLength(), random_gen);
		k = k.mod(pMinus2).add(ONE);
		BigInteger[] cipher = new BigInteger[2];
		cipher[0] = alpha.modPow(k, prime_p);
		cipher[1] = beta.modPow(k, prime_p).multiply(m).mod(prime_p);
		return cipher;
	}

	public BigInteger decrypt_base(BigInteger[] enc) {
		if (secret_a == ZERO) {
			throw new bad_gamal(2);
		}
		if (enc.length != 2) {
			throw new bad_gamal(2);
		}

		BigInteger c0 = enc[0];
		BigInteger c1 = enc[1];
		BigInteger c = c0.modPow(secret_a, prime_p).modInverse(prime_p); // c0^-a
																			// mod
																			// p
		BigInteger mm = c.multiply(c1).mod(prime_p);
		return mm;
	}

	String to_String(BigInteger[] arr) {
		if (arr.length != 2) {
			throw new bad_gamal(2);
		}
		String str = "" + arr[0] + "X" + arr[1] + "Y";
		return str;
	}

	BigInteger[] from_String(String str) {
		try {
			int idx_X = str.indexOf('X');
			if (idx_X == -1) {
				throw new bad_gamal(2);
			}
			int idx_Y = str.indexOf('Y');
			if (idx_Y == -1) {
				throw new bad_gamal(2);
			}

			if (idx_X >= idx_Y) {
				throw new bad_gamal(2);
			}

			BigInteger[] cipher = new BigInteger[2];
			cipher[0] = new BigInteger(str.substring(0, idx_X));
			cipher[1] = new BigInteger(str.substring(idx_X + 1, idx_Y));
			return cipher;
		} catch (NumberFormatException ex) {
			logger.error(ex, "during gamal derypt");
			throw new bad_gamal(2, ex.toString());
		} catch (IndexOutOfBoundsException ex) {
			logger.error(ex, "during gamal derypt");
			throw new bad_gamal(2, ex.toString());
		}
	}

	public String encrypt(BigInteger m) {
		return to_String(encrypt_base(m));
	}

	public BigInteger decrypt(String str) {
		return decrypt_base(from_String(str));
	}

	public boolean same_public_key(gamal_cipher gam) {
		boolean c1 = (prime_p.compareTo(gam.prime_p) == 0);
		boolean c2 = (alpha.compareTo(gam.alpha) == 0);
		boolean c3 = (beta.compareTo(gam.beta) == 0);
		boolean c4 = (pMinus2.compareTo(gam.pMinus2) == 0);

		boolean all_c = (c1 && c2 && c3 && c4);
		return all_c;

	}

}
