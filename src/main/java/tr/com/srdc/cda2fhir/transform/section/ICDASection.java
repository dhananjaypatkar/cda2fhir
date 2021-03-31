package tr.com.srdc.cda2fhir.transform.section;

import java.util.List;

import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public interface ICDASection {
	ISectionResult transform(IBundleInfo bundleInfo);
	
	List<ISectionResult> transformAll(IBundleInfo bundleInfo);
}
