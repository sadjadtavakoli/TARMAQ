package TARMAQ;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import TARMAQ.dataStructures.Item;
import TARMAQ.dataStructures.creators.AbstractionCreator;
import TARMAQ.dataStructures.creators.AbstractionCreator_Qualitative;
import TARMAQ.dataStructures.database.SequenceDatabase;
import TARMAQ.idlists.IDList;
import TARMAQ.idlists.creators.IdListCreator;
import TARMAQ.idlists.creators.IdListCreatorStandard_Map;
import TARMAQ.tries.TrieNode;

/**
 * TARMAQ executor
 *
 * @author sadjad
 */
public class AlgoTARMAQExecutor {

    /**
     * @param itemConstraint a list of strings/items as our item constraint
     * @param filePath       sequences where is sequences as a string or sequences
     *                       file path where they are stored.
     * @param outputPath     file path to store the result. null if want to
     *                       store in memory
     */
    public static void runFile(List<String> itemConstraint, String filePath, String outputPath)
            throws IOException {

        // sequence reading and filtreing, and association rule creation
        AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
        IdListCreator idListCreator = IdListCreatorStandard_Map.getInstance();

        SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator, idListCreator, itemConstraint);

        sequenceDatabase.loadFile(filePath);

        associationRuleRanking(sequenceDatabase);

        // Search for frequent patterns: Finished

        if (outputPath == null || outputPath.equals("-")) {
            // The user wants to save the results in memory
            System.out.println(sequenceDatabase.getStrinfiedRules());
        } else {
            // Otherwise, the user wants to save them in the given file
            try (FileWriter file = new FileWriter(outputPath, false)) {
                file.write(sequenceDatabase.getStrinfiedRules());
                file.flush();
            }
        }
    }

    public static void runList(List<String> itemConstraint, String[] sequences,
            String outputPath) throws IOException {

        String filePath = "input.txt";
        StringBuilder sequencesString = new StringBuilder();
        try (FileWriter myWriter = new FileWriter(filePath, false)) {
            for (int i = 0; i < sequences.length; i++) {
                // If the line is not a comment line
                sequencesString.append(sequences[i]).append("\n");
            }
            myWriter.write(sequencesString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        runFile(itemConstraint, filePath, outputPath);
    }

    /**
     * The actual method for extracting frequent sequences.
     *
     * @param database The original database
     */
    public static void associationRuleRanking(SequenceDatabase database) {
        // We get the initial trie whose children are the frequent 1-patterns
        List<Map<String, Object>> rules = database.getRules();
        Map<Item, TrieNode> frequentItems = database.getFrequentItems();

        final String SUPPORT = "support";
        final String CONFIDENCE = "confidence";

        for (Map<String, Object> rule : rules) {
            List<Item> antecedent = (List) rule.get("antecedent");
            Item consequent = (Item) rule.get("consequent");

            Item item = antecedent.get(0);
            TrieNode itemNode = frequentItems.get(item);
            IDList newIdList = itemNode.getChild().getIdList();
            for (int i = 1; i < antecedent.size(); i++) {
                item = antecedent.get(i);
                itemNode = frequentItems.get(item);
                newIdList = newIdList.join(itemNode.getChild().getIdList(), true);
            }
            int antecedentSupport = newIdList.getSupport();

            itemNode = frequentItems.get(consequent);
            IDList unionIdList = newIdList.join(itemNode.getChild().getIdList(), true);
            int unionSupport = unionIdList.getSupport();

            rule.put(SUPPORT, unionSupport);
            rule.put(CONFIDENCE, (double) unionSupport / antecedentSupport);
        }

        Comparator<Map<String, Object>> mapComparator = (m1, m2) -> {
            Integer supportM1 = (int) m1.get(SUPPORT);
            Integer supportM2 = (int) m2.get(SUPPORT);
            int status = supportM2.compareTo(supportM1);
            if (status == 0) {
                Double confidenceM1 = (double) m1.get(CONFIDENCE);
                Double confidenceM2 = (double) m2.get(CONFIDENCE);
                status = confidenceM2.compareTo(confidenceM1);
            }
            return status;
        };

        if (!rules.isEmpty()) {
            Collections.sort(rules, mapComparator);
            database.setRules(rules);
        }
    }
}
