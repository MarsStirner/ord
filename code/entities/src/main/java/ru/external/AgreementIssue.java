package ru.external;

import ru.entity.model.wf.HumanTaskTree;

@Deprecated
public interface AgreementIssue {

    HumanTaskTree getAgreementTree();

    void setAgreementTree(HumanTaskTree agreementTree);

}