package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.MedicationStatement;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;

public class CDAMedicationsSection implements ICDASection {
	private MedicationsSection section;

	@SuppressWarnings("unused")
	private CDAMedicationsSection() {
	};

	public CDAMedicationsSection(MedicationsSection section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<MedicationStatement> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<MedicationStatement> result = SectionResultSingular
				.getInstance(MedicationStatement.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);

		for (MedicationActivity act : section.getMedicationActivities()) {
			IEntryResult er = rt.tMedicationActivity2MedicationStatement(act, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		return result;

	}
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		return results;
	}

}
