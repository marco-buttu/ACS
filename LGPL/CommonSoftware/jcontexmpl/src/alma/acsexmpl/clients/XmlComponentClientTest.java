package alma.acsexmpl.clients;

import java.io.CharArrayReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import alma.acs.component.client.ComponentClientTestCase;
import alma.demo.XmlComponent;
import alma.demo.XmlComponentHelper;
import alma.demo.XmlComponentJ;
import alma.demo.XmlComponentOperations;
import alma.demo.XmlOffshoot;
import alma.demo.XmlOffshootJ;
import alma.demo.XmlOffshootOperations;
import alma.xmlentity.XmlEntityStruct;
import alma.xmljbind.test.obsproposal.ObsProposal;
import alma.xmljbind.test.schedblock.SchedBlock;


public class XmlComponentClientTest extends ComponentClientTestCase {

	private static final String xmlCompName = "XMLCOMP1";
	private XmlComponent xmlComponent;
	
	public XmlComponentClientTest() throws Exception {
		super("ErrorComponentTest");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		xmlComponent = XmlComponentHelper.narrow(getContainerServices().getComponent(xmlCompName));
		assertEquals(xmlCompName, xmlComponent.name());
	}

	protected void tearDown() throws Exception {
		getContainerServices().releaseComponent(xmlCompName);
		super.tearDown();
	}

	public void testOffshoot() throws Exception {

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		XmlOffshoot shoot = xmlComponent.getOffshoot();
		assertNotNull(shoot.getObsProposal());
		assertNotNull(shoot.getSchedBlock());

		// these values are hardcoded in the offshoot implementation
		XmlEntityStruct struct = shoot.getObsProposal();
		Document d = db.parse(new InputSource(new CharArrayReader(struct.xmlString.toCharArray())));
		assertEquals("rtobar", d.getElementsByTagName("PI").item(0).getTextContent());
		assertEquals("2010.0045.34S", d.getElementsByTagName("code").item(0).getTextContent());
		assertEquals("just for fun", d.getElementsByTagName("ScientificJustification").item(0).getTextContent());

		struct = shoot.getSchedBlock();
		d = db.parse(new InputSource(new CharArrayReader(struct.xmlString.toCharArray())));
		assertEquals("holography", d.getElementsByTagName("ns2:name").item(0).getTextContent());
		assertEquals("DONE", d.getElementsByTagName("ns1:status").item(0).getTextContent());
		assertEquals("true", d.getElementsByTagName("StandardMode").item(0).getTextContent());

		// just to check the setters
		shoot.setObsProposal(new XmlEntityStruct());
		shoot.setSchedBlock(new XmlEntityStruct());

		// deactivate the offshoot on the server-side
		xmlComponent.deactivateOffshoot();
		try {
			shoot.getObsProposal();
			fail("offshoot should be deactivated, I shouldn't be able to use it");
		} catch(org.omg.CORBA.OBJECT_NOT_EXIST e) {}
	}

	public void testOffshootJ() throws Exception {

		XmlOffshoot shoot = xmlComponent.getOffshoot();
		assertNotNull(shoot);
		XmlOffshootJ shootJ = getContainerServices().getTransparentXmlWrapper(XmlOffshootJ.class, shoot, XmlOffshootOperations.class);
		assertNotNull(shootJ);
		assertNotNull(shootJ.getObsProposal());
		assertNotNull(shootJ.getSchedBlock());

		// these values are hardcoded in the offshoot implementation
		ObsProposal obsProposal = shootJ.getObsProposal();
		assertEquals("rtobar", obsProposal.getPI());
		assertEquals("2010.0045.34S", obsProposal.getCode());
		assertEquals("just for fun", obsProposal.getScientificJustification());

		SchedBlock sb = shootJ.getSchedBlock();
		assertEquals("holography", sb.getName());
		assertEquals("DONE", sb.getStatus());
		assertEquals(true, sb.getStandardMode());

		// deactivate the offshoot on the server-side
		xmlComponent.deactivateOffshoot();
		try {
			shootJ.getObsProposal();
			fail("offshoot should be deactivated, I shouldn't be able to use it");
		} catch(org.omg.CORBA.OBJECT_NOT_EXIST e) {}
	}

	public void testOffshootJFromXmlComponentJ() throws Exception {

		XmlComponentJ xmlComponentJ = getContainerServices().getTransparentXmlWrapper(XmlComponentJ.class, xmlComponent, XmlComponentOperations.class);
		assertNotNull(xmlComponentJ);

		// The code below is commented since getting the offshootJ from the
		// componentJ is not yet supported.
		// Once supported, this code should work fine, and the test should pass
		/*
		XmlOffshootJ shootJ = xmlComponentJ.getOffshoot();
		assertNotNull(shootJ);
		assertNotNull(shootJ.getObsProposal());
		assertNotNull(shootJ.getSchedBlock());

		// these values are hardcoded in the offshoot implementation
		ObsProposal obsProposal = shootJ.getObsProposal();
		assertEquals("rtobar", obsProposal.getPI());
		assertEquals("2010.0045.34S", obsProposal.getCode());
		assertEquals("just for fun", obsProposal.getScientificJustification());

		SchedBlock sb = shootJ.getSchedBlock();
		assertEquals("holography", sb.getName());
		assertEquals("DONE", sb.getStatus());
		assertEquals(true, sb.getStandardMode());

		// deactivate the offshoot on the server-side
		xmlComponentJ.deactivateOffshoot();
		try {
			shootJ.getObsProposal();
			fail("offshoot should be deactivated, I shouldn't be able to use it");
		} catch(org.omg.CORBA.OBJECT_NOT_EXIST e) {}
		*/
	}
}
