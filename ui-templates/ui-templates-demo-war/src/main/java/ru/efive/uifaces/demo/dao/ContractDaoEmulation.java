package ru.efive.uifaces.demo.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import ru.efive.uifaces.demo.document.Contract;
import ru.efive.uifaces.demo.document.Organization;
import ru.efive.uifaces.demo.document.Person;

/**
 *
 * @author Denis Kotegov
 */
public class ContractDaoEmulation {

    private static final Map<Integer, Contract> store = new LinkedHashMap<Integer, Contract>();

    private static final String[] NAMES = {"Nebolshina Olga Borisovna", "Ivanov Petr Nikolaevich",
        "Nikolaev Igor Ivanovich", "Vasilyev Oleg Davidovich", "Petrova Nadezhda Ivanovna",
        "Borisova Tatyana Dmitrievna", "Davidov Yuriy Konstantinovich", "Nemyh Ilya Denisovich",
        "Petrosyan Evgeniy Vaganovich", "Turman Uma", "Kolokoltsev Igor Vladimirovich"};

    private static final String[] SHORT_NAMES = {"Nebolshina O.B.", "Ivanov P.N.",
        "Nikolaev I.I.", "Vasilyev O.D.", "Petrova N.I.",
        "Borisova T.D.", "Davidov Y.K.", "Nemyh I.D.",
        "Petrosyan E.V.", "Turman U.", "Kolokoltsev I.V."};

    private static final String[] DEPARTMENTS = {"IT", "HR", "Accounting", "Management", "Cleaning", "Manufacture"};

    private static final String[] TITLES = {"Director", "Manager", "Sale", "Driver", "Manufacturer", "Administrator"};

    private static final String[] CITIES = {"Moscow", "St.Peterspurg", "Novgorod", "Ekaterinburg", "Novosibirsk"};

    private static final String[] ORGANIZATIONS = {"MicroSoft", "Google", "Yandex", "Mail.RU", "Sberbank", "VTB"};

    private static final String[] ACTIVITIES = {"Investitions", "Saleing", "Purchasing", "Supply", "Imfrastructure"};

    private static final String[] BDDS_ITEMS = {"None", "First", "Second", "Some Item"};

    private static final String[] BUSINESS_PLAN_ITEMS = {"Credit", "Organization", "First Yeaar", "Profit"};

    private static final String[] PROJECTS = {"ASUD", "LIFT", "POWER", "NKL", "IIL"};

    private static final String ORDER_BY_ID = "id";

    private static final String ORDER_BY_AUTHOR = "author";

    private static final String ORDER_BY_CURATOR = "curator";

    private static final String ORDER_BY_SUM = "sum";

    private static final Set<String> ORDER_SET = 
            new HashSet<String>(Arrays.asList(ORDER_BY_ID, ORDER_BY_AUTHOR, ORDER_BY_CURATOR, ORDER_BY_SUM));

    private static final Random random = new Random();

    private static int counter = 1;

    private static Person createRandomPerson() {
        final int personIndex = random.nextInt(NAMES.length);
        final int departmentIndex = random.nextInt(DEPARTMENTS.length);
        final int titleIndex = random.nextInt(TITLES.length);

        Person person = new Person();
        person.setDepartment(DEPARTMENTS[departmentIndex]);
        person.setName(NAMES[personIndex]);
        person.setPhone("+7 (499) " + random.nextInt(10) + random.nextInt(10) + random.nextInt(10) + "-"
                + random.nextInt(10) + random.nextInt(10) + "-" + random.nextInt(10) + random.nextInt(10));
        person.setShortName(SHORT_NAMES[personIndex]);
        person.setTitle(TITLES[titleIndex]);

        return person;
    }

    private static Organization createRandomOrganization() {
        final int cityIndex = random.nextInt(CITIES.length);
        final int organizationIndex = random.nextInt(ORGANIZATIONS.length);

        Organization organization = new Organization();
        organization.setAddress(CITIES[cityIndex]);
        organization.setName(ORGANIZATIONS[organizationIndex]);
        organization.setPhone("+7 (495) " + random.nextInt(10) + random.nextInt(10) + random.nextInt(10) + "-"
                + random.nextInt(10) + random.nextInt(10) + "-" + random.nextInt(10) + random.nextInt(10));

        return organization;
    }

    private static void addDefaultRecord() {
        for (int i = 0; i < 189; i++) {
            final int activityIndex = random.nextInt(ACTIVITIES.length);
            final int bddsIndex = random.nextInt(BDDS_ITEMS.length);
            final int bpIndex = random.nextInt(BUSINESS_PLAN_ITEMS.length);
            final int projectIndex = random.nextInt(PROJECTS.length);

            Contract contract = new Contract();

            contract.setActivityKind(ACTIVITIES[activityIndex]);
            contract.setAuthor(createRandomPerson());
            contract.setBddsItem(BDDS_ITEMS[bddsIndex]);
            contract.setBusinessPlanItem(BUSINESS_PLAN_ITEMS[bpIndex]);
            contract.setContractKind((random.nextBoolean()? "Non ": "") + "Typical");

            int orgCount = random.nextInt(5) + 1;
            contract.setContractors(new ArrayList<Organization>(orgCount));
            for (;orgCount > 0; orgCount--) {
                contract.getContractors().add(createRandomOrganization());
            }

            contract.setCreateDate(new Date(System.currentTimeMillis() - random.nextInt(1000) * 24 * 60 * 60 * 1000));
            contract.setCurator(createRandomPerson());
            contract.setCurrency("Russian Rouble");
            contract.setPaymentTerms(random.nextBoolean() ? "Prepay " : "30% advance, 15% per month");
            contract.setProjectName(PROJECTS[projectIndex]);
            contract.setRegistrationNumber(((char) ('A' + random.nextInt(26))) + "/" + random.nextInt(1000));
            contract.setSignDate(new Date(System.currentTimeMillis() - random.nextInt(1000) * 24 * 60 * 60 * 1000));
            contract.setSum(Math.round(100 * (random.nextDouble() * 1000000d + 1d)) / 100d);
            contract.setSumWithVat(Math.round(100 * contract.getSum() * 1.18d) / 100d);
            contract.setValidThruDate(new Date(System.currentTimeMillis() + random.nextInt(1000) * 24 * 60 * 60 * 1000));

            add(contract);
        }
    }

    public static synchronized Contract get(Integer id) {
        if (store.isEmpty()) {
            addDefaultRecord();
        }

        return new Contract(store.get(id));
    }

    public static synchronized boolean delete(Integer id) {
        return store.remove(id) != null;
    }

    public static synchronized boolean save(Contract contract) {
        if (store.containsKey(contract.getId())) {
            store.put(contract.getId(), new Contract(contract));
            return true;
        } else {
            return false;
        }
    }

    public static synchronized Contract add(Contract contract) {
        Integer key = counter++;
        contract.setId(key);
        store.put(key, new Contract(contract));
        return contract;
    }

    public static synchronized List<Contract> getAll(String quickSearchString) {
        if (store.isEmpty()) {
            addDefaultRecord();
        }

        List<Contract> result = new ArrayList<Contract>(store.size());

        for (Contract contract : store.values()) {
            if (isConvenient(contract, quickSearchString)) {
                result.add(new Contract(contract));
            }
        }

        return result;
    }

    public static synchronized int getAllCount(String quickSearchString) {
        if (store.isEmpty()) {
            addDefaultRecord();
        }

        int count = 0;
        for (Contract contract : store.values()) {
            if (isConvenient(contract, quickSearchString)) {
                count++;
            }
        }

        return count;
    }

    public static synchronized List<Contract> getList(String quickSearchString, int offset, int count, String orderBy, final boolean asc) {
        List<Contract> all = getAll(quickSearchString);

        if (ORDER_SET.contains(orderBy)) {
            final boolean compareId = ORDER_BY_ID.equals(orderBy);
            final boolean compareAuthor = ORDER_BY_AUTHOR.equals(orderBy);
            final boolean compareCurator = ORDER_BY_CURATOR.equals(orderBy);

            Collections.sort(all, new Comparator<Contract>() {
                @Override
                public int compare(Contract o1, Contract o2) {
                    int result;

                    if (compareId) {
                        result = o1.getId() == o2.getId() ? 0 : (o1.getId() < o2.getId() ? -1 : 1);
                    } else if (compareAuthor) {
                        result = o1.getAuthor().getName().compareTo(o2.getAuthor().getName());
                    } else if (compareCurator) {
                        result = o1.getCurator().getName().compareTo(o2.getCurator().getName());
                    } else {
                        result = o1.getSum() == o2.getSum()? 0: (o1.getSum() < o2.getSum()? -1: 1);
                    }

                    return (asc ? 1 : -1) * result;
                }
            });
        }

        List<Contract> result = new ArrayList<Contract>(count);
        for (int i = offset; i < offset + count && i < all.size(); i++) {
            result.add(all.get(i));
        }

        return result;
    }

    public static synchronized List<Contract> getGroupedList(String quickSearchString, int offset, int count, final boolean groupCurator) {
        List<Contract> all = getAll(quickSearchString);

        Collections.sort(all, new Comparator<Contract>() {
            @Override
            public int compare(Contract o1, Contract o2) {
                int result = o1.getAuthor().getName().compareTo(o2.getAuthor().getName());
                if (result == 0 && groupCurator) {
                    result = o1.getCurator().getName().compareTo(o2.getCurator().getName());
                }
                return result;
            }
        });

        List<Contract> selection = new ArrayList<Contract>(count);
        for (int i = offset; i < offset + count && i < all.size(); i++) {
            selection.add(all.get(i));
        }

        List<Contract> result = new ArrayList<Contract>(count);
        String author = null;
        String curator = null;
        for (Contract contract : selection) {
            if (!contract.getAuthor().getName().equals(author)) {
                author = contract.getAuthor().getName();
                curator = null;

                Contract group = new Contract();
                group.setAuthor(new Person());
                group.setCurator(new Person());
                group.setId(0);
                group.getAuthor().setName(author);
                result.add(group);
            }
            if (groupCurator && !contract.getCurator().getName().equals(curator)) {
                curator = contract.getCurator().getName();

                Contract group = new Contract();
                group.setAuthor(new Person());
                group.setCurator(new Person());
                group.setId(-1);
                group.getCurator().setName(curator);
                result.add(group);
            }

            result.add(contract);
        }

        return result;
    }

    private static boolean isConvenient(Contract contract, String quickSearchString) {
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

    private ContractDaoEmulation() {

    }
}
