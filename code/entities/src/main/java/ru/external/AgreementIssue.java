package ru.external;

import ru.entity.model.wf.HumanTaskTree;

@Deprecated
public interface AgreementIssue {

    public HumanTaskTree getAgreementTree();

    public void setAgreementTree(HumanTaskTree agreementTree);

}