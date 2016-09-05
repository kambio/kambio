package emetcode.crypto.gamal;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.util.devel.logger;

public class TEST_gamal {

	public static final BigInteger ZERO = BigInteger.ZERO,
			ONE = BigInteger.ONE;

	public static void main(String[] args) {
		test_cipher(args);
	}

	public static void test_cipher(String[] args) {
		int num_bits = 1000;
		int certain = 1300;

		SecureRandom rnd = new SecureRandom();
		BigInteger msg1 = new BigInteger(num_bits, certain, rnd);
		gamal_generator gg = new gamal_generator(num_bits, certain, rnd);

		gamal_cipher gam1 = new gamal_cipher(gg, rnd);
		String pub = gam1.get_public_string();

		logger.info("pub=" + pub);

		gamal_cipher gam2 = new gamal_cipher(pub, rnd);

		String enc = gam2.encrypt(msg1);

		if (gam2.same_public_key(gam1)) {
			logger.info("SAME PUB KEY");
		} else {
			logger.info("different PUB KEY !!!!!!");
		}

		BigInteger msg2 = gam1.decrypt(enc);

		System.out.println("msg1=" + msg1);
		System.out.println("msg2=" + msg2);
	}

	// Test driver
	public static void test_generator(String[] args) {
		int bitLen = 512;
		if (args.length > 0) {
			bitLen = convert.parse_int(args[0]);
		}

		gamal_generator fact = new gamal_generator(bitLen, 300,
				new SecureRandom());
		BigInteger p = fact.get_p(), g = fact.get_g();
		System.out.println("p probable prime: "
				+ (p.isProbablePrime(300) ? "yes" : "no"));
		if (g.compareTo(p) < 0) {
			System.out.println("g < p");
		} else {
			System.out.println("p divides g: "
					+ (g.mod(p).equals(gamal_cipher.ZERO) ? "yes" : "no"));
		}
	}

	// Test whether p is prime, not p|g, and g is a generator mod p.
	public static boolean is_generator(BigInteger p, BigInteger g, int crtty) {
		System.err.printf("Testing p = %s,\ng = %s\n", p.toString(16),
				g.toString(16));

		if (!p.isProbablePrime(crtty)) {
			System.err.println("p is not prime.");
			return false;
		}
		if (g.mod(p).equals(ZERO)) {
			System.err.println("p divides g.");
			return false;
		}

		// See note below on generator test
		BigInteger pMinusOne = p.subtract(ONE), z;
		System.err.println("Finding prime factors of p-1 ...");

		// Warning: a large prime factor will take a long time to find!
		HashSet<BigInteger> factors = gamal_generator.find_factors(pMinusOne);
		boolean isGen = true;
		for (BigInteger f : factors) { // check cond on g for each f
			z = pMinusOne.divide(f);
			if (g.modPow(z, p).equals(ONE)) {
				isGen = false;
				System.err.println("g is not a generator mod p.");
				break;
			}
		}
		return isGen;
	}

}
