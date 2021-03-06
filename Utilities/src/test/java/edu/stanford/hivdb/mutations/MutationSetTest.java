/*
    
    Copyright (C) 2017 Stanford HIVDB team
    
    Sierra is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    Sierra is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.stanford.hivdb.mutations;

import org.junit.Test;

import edu.stanford.hivdb.drugs.DrugClass;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class MutationSetTest {

	@Test
	public void testParseMutationsString() {
		String nullStr = null;
		assertEquals(
			new MutationSet(),
			new MutationSet(nullStr));

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS"),
				new Mutation(Gene.RT, 210, "*"),
				new Mutation(Gene.RT, 211, "-"),
				new Mutation(Gene.RT, 212, "-")
			),
			new MutationSet(
				Gene.RT,
				"31MK, 67P: ; 69s_ss 210*; 211d+211-...212Deletion"));

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 69, "S_SS"),
				new Mutation(Gene.RT, 210, "*"),
				new Mutation(Gene.RT, 211, "-"),
				new Mutation(Gene.RT, 212, "-")),
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				null,
				new Mutation(Gene.RT, 69, "S_SS"),
				new Mutation(Gene.RT, 210, "*"),
				new Mutation(Gene.RT, 211, "-"),
				new Mutation(Gene.RT, 212, "-")));

		assertEquals(
			new MutationSet(
				new Mutation(Gene.PR, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS"),
				new Mutation(Gene.IN, 210, "*"),
				new Mutation(Gene.IN, 211, "-"),
				new Mutation(Gene.RT, 211, "-"),
				new Mutation(Gene.IN, 212, "-")
			),
			new MutationSet(
				"PR_31MK, RT67P ; RT69s_ss IN:210*, IN211d+RT211-...IN-212Deletion"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventAddAll() {
		MutationSet muts = new MutationSet();
		muts.addAll(new MutationSet("PR:31KM,RT67P"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventAdd() {
		MutationSet muts = new MutationSet();
		muts.add(new Mutation(Gene.PR, 31, "KM"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventClear() {
		MutationSet muts = new MutationSet();
		muts.clear();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventRemoveAll() {
		MutationSet muts = new MutationSet("PR:31KM,RT67P");
		muts.removeAll(new MutationSet("PR:31KM,RT67P"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventRetainAll() {
		MutationSet muts = new MutationSet("PR:31KM,RT67P");
		muts.retainAll(new MutationSet("PR:31KM,RT67P"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventRemove() {
		MutationSet muts = new MutationSet("PR:31KM");
		muts.remove(new Mutation(Gene.PR, 31, "KM"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventPollFirst() {
		MutationSet muts = new MutationSet("PR:31KM,RT67P");
		muts.pollFirst();
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testPreventPollLast() {
		MutationSet muts = new MutationSet("PR:31KM,RT67P");
		muts.pollLast();
	}

	@Test
	public void testMergesWith() {
		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS"),
				new Mutation(Gene.IN, 210, "*"),
				new Mutation(Gene.IN, 211, "-"),
				new Mutation(Gene.IN, 212, "A")
			),
			new MutationSet(Gene.RT, "31M, 67P ; 69s_ss  ")
			.mergesWith(
				new MutationSet(Gene.RT, "31K")
			)
			.mergesWith(
				new MutationSet(Gene.IN, "210* 211- ")
			)
			.mergesWith(
				new MutationSet(
					new Mutation(Gene.IN, 212, "A")
				)
			));

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P")),
			new MutationSet(Gene.RT, "31M, 67P")
			.mergesWith(new Mutation(Gene.RT, 31, "K")));

	}

	@Test
	public void testIntersectsWith() {
		MutationSet self = new MutationSet(Gene.RT, "48VER");
		MutationSet another = new MutationSet(Gene.RT, "48E,48AR,36K");
		assertEquals(
			new MutationSet(Gene.RT, "48ER"),
			self.intersectsWith(another));

		assertEquals(
			new MutationSet(Gene.RT, "48ER"),
			another.intersectsWith(self));

		assertEquals(
			new MutationSet(Gene.RT, "48RV"),
			self.intersectsWith(new Mutation(Gene.RT, 48, "VR")));

		assertEquals(
			new MutationSet(),
			self.intersectsWith(new Mutation(Gene.RT, 36, "AK")));

		assertEquals(
			new MutationSet("RT:36K"),
			another.intersectsWith(new Mutation(Gene.RT, 36, "AK")));

	}

	@Test
	public void testSubtractsBy() {
		MutationSet self = new MutationSet("RT:48VER PR48VER PR:32E");
		MutationSet another = new MutationSet("RT:48E,RT:48AR,RT:36K,PR33D");
		assertEquals(
			new MutationSet("RT:48V PR:32E PR:48VER"),
			self.subtractsBy(another));

		assertEquals(
			new MutationSet("RT:48A RT:36K PR:33D"),
			another.subtractsBy(self));

		assertEquals(
			new MutationSet("RT:48E PR:32E PR:48VER"),
			self.subtractsBy(Arrays.asList(new Mutation[] {
				new Mutation(Gene.RT, 48, "VR"),
				new Mutation(Gene.RT, 32, "E")
			})));

		assertEquals(
			new MutationSet("RT:48E PR:32E PR:48VER"),
			self.subtractsBy(new Mutation(Gene.RT, 48, "VR")));

		assertEquals(
			new MutationSet("RT:48VER PR:48VER PR:32E"),
			self.subtractsBy(new Mutation(Gene.RT, 36, "AK")));

		assertEquals(
			new MutationSet("RT:48ARE PR:33D"),
			another.subtractsBy(new Mutation(Gene.RT, 36, "AK")));
	}

	@Test
	public void testGetNonSdrmTsms() {
		MutationSet sdrms = new MutationSet(
			"RT_M41L,RT_L74I,RT_V75M,RT_V179F,RT_M184V");
		MutationSet tsms = new MutationSet(
			"RT_M41L,RT_K65N,RT_D67S,RT_K70Q,RT_L74I,RT_V75M,RT_I94L," +
			"RT_K101H,RT_V179F,RT_M184V,RT_G190Q,RT_H208Y,RT_H221Y");

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 65, "N"),
				new Mutation(Gene.RT, 67, "S"),
				new Mutation(Gene.RT, 70, "Q"),
				new Mutation(Gene.RT, 94, "L"),
				new Mutation(Gene.RT, 101, "H"),
				new Mutation(Gene.RT, 190, "Q"),
				new Mutation(Gene.RT, 208, "Y"),
				new Mutation(Gene.RT, 221, "Y")),
			tsms.subtractsBy(sdrms));
	}

	@Test
	public void testHasInsertionAt() {
		assertTrue(
			new MutationSet(Gene.RT, "31KM, 67P, 69S_SS")
			.hasInsertionAt(Gene.RT, 69));
		assertFalse(
			new MutationSet(Gene.RT, "31KM, 67P, 68S_SS")
			.hasInsertionAt(Gene.RT, 69));
		assertFalse(
			new MutationSet(Gene.RT, "31KM, 67P, 69W")
			.hasInsertionAt(Gene.RT, 69));
	}

	@Test
	public void testHasDeletionAt() {
		assertTrue(
			new MutationSet(Gene.RT, "31KM, 67P, 69-")
			.hasDeletionAt(Gene.RT, 69));
		assertFalse(
			new MutationSet(Gene.RT, "31KM, 67P, 68-")
			.hasDeletionAt(Gene.RT, 69));
		assertFalse(
			new MutationSet(Gene.RT, "31KM, 67P, 69W")
			.hasDeletionAt(Gene.RT, 69));
		assertFalse(
			new MutationSet(Gene.RT, "31KM, 67P, 69S_SS")
			.hasDeletionAt(Gene.RT, 69));
	}

	@Test
	public void testGet() {
		assertEquals(
			new Mutation(Gene.RT, 69, "S_SS"),
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).get(Gene.RT, 69));

		assertEquals(
			new Mutation(Gene.RT, 31, "KM"),
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).get(Gene.RT, 31));

		assertEquals(
			null,
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).get(Gene.RT, 37));

		assertEquals(
			null,
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.PR, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).get(Gene.RT, 67));

		assertEquals(
			new Mutation(Gene.PR, 67, "P"),
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "K"),
				new Mutation(Gene.PR, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).get(Gene.PR, 67));
	}

	@Test
	public void testCompareTwoSets() {
		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			));

		assertNotEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "I"),
				new Mutation(Gene.RT, 69, "S_SS")
			));
	}

	@Test
	public void testAutoMerge() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			));

		assertEquals(
			new Mutation(Gene.RT, 31, "KM"),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).get(Gene.RT, 31));
	}

	@Test
	public void testGetGeneMutations() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 31, "KM"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.PR, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.IN, 31, "M"),
				new Mutation(Gene.RT, 67, "P"),
				new Mutation(Gene.RT, 69, "S_SS")
			).getGeneMutations(Gene.RT));
	}

	@Test
	public void testGetInsertions() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.RT, 67, "_A"),
				new Mutation(Gene.RT, 69, "S_SS")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.IN, 31, "-"),
				new Mutation(Gene.RT, 67, "_A"),
				new Mutation(Gene.RT, 69, "S_SS")
			).getInsertions());
	}

	@Test
	public void testGetDeletions() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.IN, 31, "-")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.IN, 31, "-"),
				new Mutation(Gene.RT, 67, "_A"),
				new Mutation(Gene.RT, 69, "S_SS")
			).getDeletions());
	}

	@Test
	public void testGetStopCodons() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.IN, 31, "*")
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.IN, 31, "*"),
				new Mutation(Gene.RT, 67, "-"),
				new Mutation(Gene.RT, 69, "S_SS")
			).getStopCodons());
	}

	@Test
	public void testGetAmbiguousCodons() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.IN, 31, "X"),
				new Mutation(Gene.PR, 77, "P") // TODO: should this be ambiguous?
			),
			new MutationSet(
				new Mutation(Gene.RT, 31, "K"),
				new Mutation(Gene.RT, 31, "M"),
				new Mutation(Gene.IN, 31, "X"),
				new Mutation(Gene.PR, 77, "P", "CCV"),
				new Mutation(Gene.RT, 67, "*"),
				new Mutation(Gene.RT, 69, "S_SS")
			).getAmbiguousCodons());
	}

	@Test
	public void testGetUnusualMutations() {
		MutationSet muts = new MutationSet(
			new Mutation(Gene.RT, 69, "KS"),
			new Mutation(Gene.PR, 82, "VIA"),
			new Mutation(Gene.RT, 67, "NW"),
			new Mutation(Gene.PR, 13, "F"),
			new Mutation(Gene.PR, 14, "F"),
			new Mutation(Gene.PR, 15, "F"),
			new Mutation(Gene.PR, 16, "F"));
		MutationSet expected = new MutationSet(
			new Mutation(Gene.RT, 67, "NW"),
			new Mutation(Gene.PR, 13, "F"),
			new Mutation(Gene.PR, 14, "F"),
			new Mutation(Gene.PR, 15, "F"),
			new Mutation(Gene.PR, 16, "F"));
		assertEquals(expected, muts.getUnusualMutations());
	}

	@Test
	public void testGetHighestMutPrevalences() {
		MutationSet muts = new MutationSet("RT:67N,RT:69KS,PR:82VIA,RT68W");
		Map<Mutation, Double> expected = new HashMap<>();
		expected.put(new Mutation(Gene.RT, 67, "N"), 10.373);
		expected.put(new Mutation(Gene.RT, 69, "KS"), 0.813);
		expected.put(new Mutation(Gene.PR, 82, "VIA"), 5.33);
		expected.put(new Mutation(Gene.RT, 68, "W"), 0.0);
		assertEquals(expected, muts.getHighestMutPrevalences());
	}

	@Test
	public void testGroupByMutType() {
		MutationSet sequenceMuts = new MutationSet(
			new Mutation(Gene.RT, 65, "N"),
			new Mutation(Gene.RT, 115, "FR"),
			new Mutation(Gene.RT, 118, "I"),
			new Mutation(Gene.RT, 103, "N"),
			new Mutation(Gene.RT, 41, "P"),
			new Mutation(Gene.PR, 84, "V"),
			new Mutation(Gene.IN, 155, "S"));
		Map<MutType, MutationSet> expected = new EnumMap<>(MutType.class);
		expected.put(MutType.Major, new MutationSet("PR84V"));
		expected.put(MutType.Accessory, new MutationSet());
		expected.put(MutType.Other, new MutationSet());

		assertEquals(expected, sequenceMuts.groupByMutType(Gene.PR));

		expected = new EnumMap<>(MutType.class);
		expected.put(MutType.NRTI, new MutationSet("RT65N,RT115FR"));
		expected.put(MutType.NNRTI, new MutationSet("RT103N"));
		expected.put(MutType.Other, new MutationSet("RT41P,RT118I"));

		assertEquals(expected, sequenceMuts.groupByMutType(Gene.RT));

		expected = new EnumMap<>(MutType.class);
		expected.put(MutType.Major, new MutationSet("IN155S"));
		expected.put(MutType.Accessory, new MutationSet());
		expected.put(MutType.Other, new MutationSet());

		assertEquals(expected, sequenceMuts.groupByMutType(Gene.IN));
	}

	@Test
	public void testGroupByGene() {
		MutationSet sequenceMuts = new MutationSet(
			new Mutation(Gene.RT, 65, "N"),
			new Mutation(Gene.RT, 115, "FR"),
			new Mutation(Gene.RT, 118, "I"),
			new Mutation(Gene.RT, 103, "N"),
			new Mutation(Gene.RT, 41, "P"),
			new Mutation(Gene.PR, 84, "V"),
			new Mutation(Gene.IN, 155, "S"));
		Map<Gene, MutationSet> expected = new EnumMap<>(Gene.class);
		expected.put(Gene.RT, new MutationSet("RT65N,RT115FR,RT118I,RT103N,RT41P"));
		expected.put(Gene.PR, new MutationSet("PR84V"));
		expected.put(Gene.IN, new MutationSet("IN155S"));
		assertEquals(expected, sequenceMuts.groupByGene());
	}

	@Test
	public void testGetAtDRPMutations() {
		MutationSet sequenceMuts = new MutationSet(
			new Mutation(Gene.RT, 68, "A"),
			new Mutation(Gene.RT, 115, "FR"),
			new Mutation(Gene.RT, 118, "I"),
			new Mutation(Gene.RT, 103, "N"),
			new Mutation(Gene.RT, 41, "P"),
			new Mutation(Gene.PR, 84, "K"),
			new Mutation(Gene.IN, 155, "S"));
		MutationSet expected = new MutationSet(
			"PR84K,RT41P,RT68A,RT103N,RT115FR,IN155S");
		assertEquals(expected, sequenceMuts.getAtDRPMutations());
	}

	@Test
	public void testGetDRMs() {
		MutationSet sequenceMuts = new MutationSet(
			new Mutation(Gene.RT, 68, "A"),
			new Mutation(Gene.RT, 115, "FR"),
			new Mutation(Gene.RT, 118, "I"),
			new Mutation(Gene.RT, 103, "N"),
			new Mutation(Gene.RT, 41, "P"),
			new Mutation(Gene.PR, 84, "KV"),
			new Mutation(Gene.IN, 155, "S"));
		MutationSet expected = new MutationSet("PR84KV");
		assertEquals(expected, sequenceMuts.getDRMs(DrugClass.PI));
		expected = new MutationSet("RT115FR");
		assertEquals(expected, sequenceMuts.getDRMs(DrugClass.NRTI));
		expected = new MutationSet("RT103N");
		assertEquals(expected, sequenceMuts.getDRMs(DrugClass.NNRTI));
	}

	@Test
	public void testJoin() {
		MutationSet sequenceMuts = new MutationSet(
			new Mutation(Gene.RT, 65, "P"),
			new Mutation(Gene.RT, 115, "FR"),
			new Mutation(Gene.RT, 67, "Deletion"),
			new Mutation(Gene.RT, 69, "Insertion"),
			new Mutation(Gene.PR, 84, "KV"),
			new Mutation(Gene.IN, 155, "S"));
		assertEquals(
			"PR_I84KV, RT_K65P, RT_D67Deletion, " +
			"RT_T69Insertion, RT_Y115FR, IN_N155S",
			sequenceMuts.join(", ", Mutation::getHumanFormatWithGene));
		assertEquals(
			"PR_I84KV+RT_K65P+RT_D67Deletion+RT_T69Insertion+RT_Y115FR+IN_N155S",
			sequenceMuts.join('+', Mutation::getHumanFormatWithGene));
		assertEquals(
			"I84KV+K65P+D67Deletion+T69Insertion+Y115FR+N155S",
			sequenceMuts.join('+'));
		assertEquals(
			"I84KV, K65P, D67Deletion, T69Insertion, Y115FR, N155S",
			sequenceMuts.join(", "));
		assertEquals(
			"PR_I84KV,RT_K65P,RT_D67Deletion,RT_T69Insertion,RT_Y115FR,IN_N155S",
			sequenceMuts.join(Mutation::getHumanFormatWithGene));
	}

	@Test
	public void testToStringList() {
		MutationSet sequenceMuts = new MutationSet(
			new Mutation(Gene.RT, 65, "P"),
			new Mutation(Gene.RT, 115, "FR"),
			new Mutation(Gene.RT, 67, "Deletion"),
			new Mutation(Gene.RT, 69, "Insertion"),
			new Mutation(Gene.PR, 84, "KV"),
			new Mutation(Gene.IN, 155, "S"));
		assertEquals(
			Arrays.asList(new String[] {
				"I84KV",
				"K65P",
				"D67Deletion",
				"T69Insertion",
				"Y115FR",
				"N155S"
			}),
			sequenceMuts.toStringList());

		assertEquals(
			Arrays.asList(new String[] {
				"I84KV",
				"K65P",
				"D67d",
				"T69i",
				"Y115FR",
				"N155S"
			}),
			sequenceMuts.toASIFormat());
	}

	@Test
	public void testHashCode() {

		assertEquals(
			new MutationSet(
				new Mutation(Gene.PR, 31, "X"),
				new Mutation(Gene.PR, 77, "P")
			).hashCode(),
			new MutationSet(Gene.PR, "31X 77P").hashCode()
		);

		assertNotEquals(
			new MutationSet(Gene.PR, "31X"),
			new MutationSet(Gene.RT, "31X")
		);
	}
}
