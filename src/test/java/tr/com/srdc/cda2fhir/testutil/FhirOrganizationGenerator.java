package tr.com.srdc.cda2fhir.testutil;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Organization.OrganizationContactComponent;
import org.hl7.fhir.r4.model.codesystems.ContactentityType;
import org.hl7.fhir.r4.model.codesystems.OrganizationType;

public class FhirOrganizationGenerator {
	public Organization generate() {
		Organization organization = new Organization();
		organization.setName("Aperture Science");
		organization.setActive(true);

		Coding typeCoding = new Coding(OrganizationType.BUS.getSystem(), OrganizationType.BUS.toCode(),
				OrganizationType.BUS.getDisplay());
		organization.addType(new CodeableConcept().addCoding(typeCoding));

		OrganizationContactComponent occ = new OrganizationContactComponent();
		Coding purposeCoding = new Coding(ContactentityType.ADMIN.getSystem(), ContactentityType.ADMIN.toCode(),
				ContactentityType.ADMIN.getDisplay());
		occ.setPurpose(new CodeableConcept().addCoding(purposeCoding));

		Address address = new Address();
		address.addLine("100 Aperture Drive");
		address.setCity("Cleveland");
		address.setState("Ohio");
		address.setPostalCode("44101");
		occ.setAddress(address);

		organization.addContact(occ);
		return organization;
	}
}