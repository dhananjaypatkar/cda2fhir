package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.openhealthtools.mdht.uml.cda.consol.EncountersSection;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAEncountersSection implements ICDASection {
	private EncountersSection section;

	@SuppressWarnings("unused")
	private CDAEncountersSection() {
	};

	public CDAEncountersSection(EncountersSection section) {
		this.section = section;
	}

	@Override
	public ISectionResult transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformEncounterActivitiesList(section.getEncounterActivitiess(), bundleInfo);
	}
	
	/*public ISectionResult transformCommunication(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformEncounterActivitiesListToCommunication(section.getEncounterActivitiess(), bundleInfo);
	}*/
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		//results.add(transformCommunication(bundleInfo));
		return results;
	}
}
