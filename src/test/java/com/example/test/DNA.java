package com.example.test;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class DNA {

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

	public static long encodeSequenceNucleotideComplementaryToValue(byte[] sequence, int start, int length) {
		long value = 0;
		long pow = 1;
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
		// get input N from terminal
		int N = scanner.nextInt();
		System.out.println();
		byte[] arrayDNA = new byte[N];
		// generate random sequence DNA
		for (int i = 0; i < arrayDNA.length; i++) {
			arrayDNA[i] = (byte) ThreadLocalRandom.current().nextInt(base);
		}
		// display it
		System.out.println("Random Sequence DNA is : ");
		for (int i = 0; i < arrayDNA.length; i++) {
			System.out.print(decodeNucleotide(arrayDNA[i]));
		}
		System.out.println();
		
		for (int i = 0; i < arrayDNA.length; i++) {
			System.out.print(getNucleotideComplementary(arrayDNA[i]));
		}
		System.out.println();
		System.out.println();
		System.out.print("Enter length L : ");
		// get input L from terminal
		int L = scanner.nextInt();
		System.out.println();

		// create array for find sub-sequence of size =L which repeated the most often
		// and max repeated
		long[] convertedValueDNA = new long[2 * (N - L + 1)];

		// convert sequence DNA to array long with base-4
		for (int i = 0; i < N - L + 1; i++) {
			convertedValueDNA[i] = encodeSequenceNucleotidesToValue(arrayDNA, i, L);
		}
		for (int i = 0; i < N - L + 1; i++) {
			convertedValueDNA[N - L + 1 + i] = encodeSequenceNucleotideComplementaryToValue(arrayDNA, i, L);
		}

		// show all sub-sequence of size = L
		System.out.println("We have " + convertedValueDNA.length + " sub-sequences of size = " + L
				+ " \t You press Enter to show next one .( Please don't hold  Enter )");
		scanner.nextLine();
		for (int i = 0; i < convertedValueDNA.length; i++) {
			System.out.print(decodeSequenceNucleotidesFromValueToChar(convertedValueDNA[i], L));
			scanner.nextLine();
		}

		System.out.println();
		for (int i = 0; i < convertedValueDNA.length; i++) {
			System.out.print(convertedValueDNA[i] +" ");
		}
		
		System.out.println();
		
		// quick sort array increase
		quickSort(convertedValueDNA, 0, convertedValueDNA.length - 1);

		System.out.println();
		for (int i = 0; i < convertedValueDNA.length; i++) {
			System.out.print(convertedValueDNA[i] +" ");
		}
		
		System.out.println();
		// find value max repeated the most often and list value
		ArrayList<Long> maxRepeatedValue = new ArrayList<Long>();
		int i = 0, max = 0;
		int frequency;
		while (i < convertedValueDNA.length) {
			frequency = 1;
			while ((i < convertedValueDNA.length - 1) && (convertedValueDNA[i] == convertedValueDNA[i + 1])) {
				frequency++;
				i++;
			}
			if (max < frequency) {
				max = frequency;
				maxRepeatedValue = new ArrayList<Long>();
				maxRepeatedValue.add(convertedValueDNA[i]);
			} else if (max == frequency)
				maxRepeatedValue.add(convertedValueDNA[i]);
			i++;
		}

		System.out.println("We have " + maxRepeatedValue.size() + " sub-sequence of size = " + L
				+ " repeated the most often with max repeated = " + max);

		// show all value repeated the most often and convert it to sub-sequence
		for (Long value : maxRepeatedValue) {
			System.out.println(decodeSequenceNucleotidesFromValueToChar(value, L));
		}

		scanner.close();

	}

}
