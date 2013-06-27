package ru.efive.uifaces.demo.dao;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ru.efive.uifaces.demo.document.Contract;
import ru.efive.uifaces.demo.document.Person;

/**
 * <code>ContractDaoEmulationTest</code> is test class for class
 * <code>ContractDaoEmulation</code>.
 * 
 * @author Ramil_Habirov
 */
public class ContractDaoEmulationTest {

    private static final String QUICK_SEARCH_STRING_NAME = "Nebolshina Olga Borisovna";

    /**
     * Tests <code>getAll</code> method of <code>ContractDaoEmulation</code>.
     */
    @Test
    public void testGetAll() {
        List<Contract> contracts = ContractDaoEmulation
                .getAll(QUICK_SEARCH_STRING_NAME);
        for (Contract contract : contracts) {
            assertEquals(true, isConvenient(contract, QUICK_SEARCH_STRING_NAME));
        }
    }

    /**
     * Tests <code>getAllCount</code> method of
     * <code>ContractDaoEmulation</code>.
     */
    @Test
    public void getAllCount() {
        List<Contract> contracts = ContractDaoEmulation
                .getAll(QUICK_SEARCH_STRING_NAME);
        int count = ContractDaoEmulation.getAllCount(QUICK_SEARCH_STRING_NAME);
        assertEquals(contracts.size(), count);
    }

    /**
     * Tests <code>getList</code> method of <code>ContractDaoEmulation</code>.
     */
    @Test
    public void getList() {
        List<Contract> contracts = ContractDaoEmulation.getList(
                QUICK_SEARCH_STRING_NAME, 0, 10, "id", true);
        for (Contract contract : contracts) {
            assertEquals(true, isConvenient(contract, QUICK_SEARCH_STRING_NAME));
        }
    }

    /**
     * Tests <code>getGroupedList</code> method of
     * <code>ContractDaoEmulation</code>.
     */
    @Test
    public void getGroupedList() {
        List<Contract> contracts = ContractDaoEmulation.getGroupedList(
                QUICK_SEARCH_STRING_NAME, 0, 10, true);
        for (Contract contract : contracts) {
            if (contract.getId() > 0) {
                assertEquals(true,
                        isConvenient(contract, QUICK_SEARCH_STRING_NAME));
            }
        }
    }

    private boolean isConvenient(Contract contract, String quickSearchString) {
        if (contract == null) {
            throw new IllegalArgumentException("Contract must not be null.");
        }

        if (quickSearchString == null || quickSearchString.isEmpty()) {
            return true;
        }

        String quickSearchStringInUpperCase = quickSearchString.toUpperCase();

        Integer contractId = contract.getId();
        if (contractId != null
                && contractId.toString().toUpperCase()
                        .contains(quickSearchStringInUpperCase)) {
            return true;
        }

        if (contract.getAuthor() != null) {
            Person contractAuthor = contract.getAuthor();
            String contractAuthorName = contractAuthor.getName();
            if (contractAuthorName != null
                    && contractAuthorName.toUpperCase().contains(
                            quickSearchStringInUpperCase)) {
                return true;
            }
            String contractAuthorShortName = contractAuthor.getShortName();
            if (contractAuthorShortName != null
                    && contractAuthorShortName.toUpperCase().contains(
                            quickSearchStringInUpperCase)) {
                return true;
            }
        }

        if (contract.getCurator() != null) {
            Person contractCurator = contract.getCurator();
            String contractCuratorName = contractCurator.getName();
            if (contractCuratorName != null
                    && contractCuratorName.toUpperCase().contains(
                            quickSearchStringInUpperCase)) {
                return true;
            }
            String contractCuratorShortName = contractCurator.getShortName();
            if (contractCuratorShortName != null
                    && contractCuratorShortName.toUpperCase().contains(
                            quickSearchStringInUpperCase)) {
                return true;
            }
        }

        Double contractSum = contract.getSum();
        if (contractSum != null
                && contractSum.toString().toUpperCase()
                        .contains(quickSearchStringInUpperCase)) {
            return true;
        }

        return false;
    }
}
