package com.example.test;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class DNA2 {

	public static byte base = 4;

	public static int partition(long arr[], int low, int high) {
		long pivot = arr[high];
		int i = (low - 1); // index of smaller element
		for (int j = low; j < high; j++) {
			// If current element is smaller than or
			// equal to pivot
			if (arr[j] <= pivot) {
				i++;

				// swap arr[i] and arr[j]
				long temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
			}
		}

		// swap arr[i+1] and arr[high] (or pivot)
		long temp = arr[i + 1];
		arr[i + 1] = arr[high];
		arr[high] = temp;
		return i + 1;
	}

	/*
	 * The main function that implements QuickSort() arr[] --> Array to be sorted,
	 * low --> Starting index, high --> Ending index
	 */
	public static void quickSort(long arr[], int low, int high) {
		if (low < high) {
			/*
			 * pi is partitioning index, arr[pi] is now at right place
			 */
			int pi = partition(arr, low, high);
			// Recursively sort elements before
			// partition and after partition
			quickSort(arr, low, pi - 1);
			quickSort(arr, pi + 1, high);
		}
	}

	public static char decodeNucleotide(byte number) {
		switch (number) {
		case 0:
			return 'A';
		case 1:
			return 'C';
		case 2:
			return 'G';
		case 3:
			return 'T';
		default:
			throw new IllegalArgumentException("invalid id = " + number);
		}
	}

	public static char getNucleotideComplementary(byte number) {
		return decodeNucleotide((byte) (base - number - 1));
	}

	public static int encodeSequenceNucleotideComplementaryToValue(byte[] sequence, int start, int length) {
		int value = 0;
		int pow = 1;
		for (int i = length - 1; i >= 0; i--) {
			value = value + pow * (base - sequence[start + i] - 1);
			pow = pow * base;
		}
		return value;
	}

	public static long encodeSequenceNucleotidesToValue(byte[] sequence, int start, int length) {
		long value = 0;
		long pow = 1;
		for (int i = length - 1; i >= 0; i--) {
			value = value + pow * sequence[start + i];
			pow = pow * base;
		}
		return value;
	}

	public static byte[] decodeSequenceNucleotidesFromValue(int value, int length) {
		byte[] sequence = new byte[length];
		while (length > 0) {

			sequence[length - 1] = (byte) (value % base);
			value = value / base;
			length = length - 1;
		}
		return sequence;
	}

	public static char[] decodeSequenceNucleotidesFromValueToChar(long value, int length) {
		char[] sequence = new char[length];
		while (length > 0) {
			byte remainder = (byte) (value % base);
			// System.out.print(remainder+" ");
			sequence[length - 1] = decodeNucleotide(remainder);
			value = value / base;
			length = length - 1;
		}
		return sequence;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter number N : ");
		// get their input as a String
		int N = scanner.nextInt();
		System.out.println();
		byte[] arrayDNA = new byte[N];
		for (int i = 0; i < arrayDNA.length; i++) {
			arrayDNA[i] = (byte) ThreadLocalRandom.current().nextInt(base);
		}

		System.out.println("Sequence DNA is : ");
		for (int i = 0; i < arrayDNA.length; i++) {
			// System.out.print("-" + decodeNucleotide(arrayDNA[i]) + "-");
		}
		System.out.println();

		for (int i = 0; i < arrayDNA.length; i++) {
			// System.out.print("-" + getNucleotideComplementary(arrayDNA[i]) + "-");
		}
		System.out.println();
		System.out.print("Enter length L : ");
		int L = scanner.nextInt();
		System.out.println();

		long[] convertedValueDNA = new long[2 * (N - L + 1)];

		for (int i = 0; i < N - L + 1; i++) {
			convertedValueDNA[i] = encodeSequenceNucleotidesToValue(arrayDNA, i, L);
		}

		for (int i = 0; i < N - L + 1; i++) {
			convertedValueDNA[N - L + 1 + i] = encodeSequenceNucleotideComplementaryToValue(arrayDNA, i, L);
		}

		System.out.println(convertedValueDNA.length);
		long start = System.currentTimeMillis();
		HashMap<Long, Integer> countRepeated = new HashMap<>();
		for (int i = 0; i < convertedValueDNA.length; i++) {
			long value = convertedValueDNA[i];
			Integer count = countRepeated.get(value);
			if (count == null) {
				count = 1;
				countRepeated.put(value, count);
			} else {
				count = count + 1;
				countRepeated.put(value, count);
			}
		}

		System.out.println(countRepeated.size());
		int max = 0;
		for (int v : countRepeated.values()) {
			if (v > max)
				max = v;
		}

		System.out.println("max = " + max);

		for (Long key : countRepeated.keySet()) {
			if (countRepeated.get(key) == max)
				System.out.println(decodeSequenceNucleotidesFromValueToChar(key, L));
		}

		System.out.println(" caculate time = " + (System.currentTimeMillis() - start));
		scanner.close();
		// int n = 100;
		// int l = 10;
		// for (long i = 0; i < 1000000; i++) {
		//// byte[] s = decodeSequenceNucleotidesFromValue(i, l);
		//// long encode = encodeSequenceNucleotidesToValue(s);
		// System.err.println(i+" \t " +
		// String.valueOf(decodeSequenceNucleotidesFromValueToChar(i, l) ));
		//// break;
		// }

	}

}
