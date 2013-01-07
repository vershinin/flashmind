package net.vershinin.flashmind;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.wizards.AbstractMindMapExportPage;

public class FreeMindExportWizardPage extends AbstractMindMapExportPage {
	private static final String PAGE_NAME = "org.xmind.ui.export.flashExportWizard"; //$NON-NLS-1$

	protected FreeMindExportWizardPage() {
		super(PAGE_NAME, WizardMessages.FreeMindPage_title);
		setDescription(WizardMessages.FreeMindPage_description);
	}

	@Override
	protected String getSuggestedFileName() {
		IMindMap mindMap = getCastedWizard().getSourceMindMap();
		String fileName = mindMap.getCentralTopic().getTitleText();
		return fileName + FlashExportWizard.HTML_EXT;
	}

	protected FlashExportWizard getCastedWizard() {
		return (FlashExportWizard) super.getCastedWizard();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		composite.setLayout(layout);
		setControl(composite);

		Control fileGroup = createFileControls(composite);
		fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		updateStatus();
	}

	@Override
	protected void setDialogFilters(FileDialog dialog, List<String> filterNames, List<String> filterExtensions) {
		filterNames.add(0, WizardMessages.HtmlExportPage_FileDialog_HTMLFile);
		filterExtensions.add(0, "*" + FlashExportWizard.HTML_EXT); //$NON-NLS-1$
		super.setDialogFilters(dialog, filterNames, filterExtensions);
	}
}