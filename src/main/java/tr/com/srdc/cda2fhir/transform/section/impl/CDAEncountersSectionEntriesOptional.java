package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Encounter;
import org.openhealthtools.mdht.uml.cda.consol.EncountersSectionEntriesOptional;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAEncountersSectionEntriesOptional implements ICDASection {
	private EncountersSectionEntriesOptional section;

	@SuppressWarnings("unused")
	private CDAEncountersSectionEntriesOptional() {
	};

	public CDAEncountersSectionEntriesOptional(EncountersSectionEntriesOptional section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Encounter> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformEncounterActivitiesList(section.getEncounterActivitiess(), bundleInfo);
	}
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		return results;
	}
}
