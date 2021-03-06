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

package edu.stanford.hivdb.graphql;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import edu.stanford.hivdb.filetestutils.TestSequencesFiles;
import edu.stanford.hivdb.filetestutils.TestSequencesFiles.TestSequencesProperties;
import edu.stanford.hivdb.utilities.FastaUtils;
import edu.stanford.hivdb.utilities.Json;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.InvalidSyntaxError;

public class SierraSchemaTest {

	@Test
	public void testSequenceAnalysisDataFetcher() {
		Map<String, Object> arguments = new LinkedHashMap<>();
		List<Map<String, String>> sequences =
			FastaUtils.readStream(TestSequencesFiles.getTestSequenceInputStream(TestSequencesProperties.PROBLEM_SEQUENCES))
			.stream()
			.map(seq -> {
				Map<String, String> seqMap = new LinkedHashMap<>();
				seqMap.put("header", seq.getHeader());
				seqMap.put("sequence", seq.getSequence());
				return seqMap;
			})
			.collect(Collectors.toList());
		arguments.put("sequences", sequences);
		Object context = null;

		ExecutionResult result = new GraphQL(SierraSchema.schema)
			.execute(
			"query ($sequences: [UnalignedSequenceInput]) {\n" +
			"  viewer {\n" +
			"  genes { name mutationTypes }\n" +
			"  sequenceAnalysis(sequences: $sequences) {\n" +
			"    inputSequence { header MD5 }\n" +
			"    validationResults { level message }\n" +
			"    availableGenes { name }\n" +
			"    mutations {\n" +
			"      gene { name }\n" +
			"      consensus\n" +
			"      position\n" +
			"      AAs\n" +
			"      triplet\n" +
			"      isInsertion\n" +
			"      isDeletion\n" +
			"      isIndel\n" +
			"      isAmbiguous\n" +
			"      isApobecMutation\n" +
			"      isApobecDRM\n" +
			"      hasStop\n" +
			"      isUnusual\n" +
			"      types\n" +
			"      primaryType\n" +
			"      comments { consensus triggeredAAs type text }\n" +
			"      shortText\n" +
			"      text\n" +
			"    }\n" +
			"    mixturePcnt\n" +
			"    subtypes {name, distancePcnt} \n" +
			"    subtypeText\n" +
			"    absoluteFirstNA\n" +
			"    frameShifts {gene {name}, position, isInsertion, isDeletion, size, NAs, text}\n" +
			/*"    alignedGeneSequences {\n" +
			"       gene {name length }\n" +
			"       firstNA lastNA firstAA lastAA\n" +
			"       matchPcnt frameShifts { text }\n" +
			"       mutations(filterOptions: [INSERTION]) { text }\n" +
			"       prettyPairwise { mutationLine }\n" +
			"    }\n" +
			"    drugResistance {" +
			"      gene {name, drugClasses {name fullName drugs {name fullName drugClass {name}} } }" +
			"      mutationsByTypes {mutationType mutations {text} } \n" +
			"      drugScores { drugClass{name} drug{name} level score text SIR, partialScores {mutations {text}, score} }\n" +
			"    }\n" +*/
			"  }\n" +
			//"  alignedSequenceSpreadsheet {header body}\n" +
			//"    mutationPrevalenceSubtypes { name stats {gene{name} totalNaive totalTreated} }\n" +
			//"mutationsAnalysis(mutations:[\"RT:122*\",\"RT:133*\",\"RT:144X\",\"RT:155X\"]) {\n" +
			/*"    mutationPrevalences {\n" +
			"      boundMutation {text}\n" +
			"	   matched { AA subtypes {\n" +
			"        percentageNaive\n" +
			"        percentageTreated\n" +
			"        subtype {name  } } }\n" +
			"	   others { AA subtypes { subtype {name} } }\n" +
			"    }\n" +*/
			//"  validationResults { level message }\n" +
			//"}\n" +
			"  }\n" +
			"}", context, arguments);
		for (GraphQLError error : result.getErrors()) {
			if (error instanceof ExceptionWhileDataFetching) {
				((ExceptionWhileDataFetching) error).getException().printStackTrace();
			}
			else if (error instanceof InvalidSyntaxError) {
				System.err.println(((InvalidSyntaxError) error).getMessage());
				System.err.println(((InvalidSyntaxError) error).getLocations());
			}
		}
		assertTrue(
			"Found errors in query: " + Json.dumps(result.getErrors()),
			result.getErrors().isEmpty());
		System.out.println(Json.dumps(result.getData()));
	}

}
