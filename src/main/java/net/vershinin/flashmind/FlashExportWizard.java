/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.vershinin.flashmind;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.AbstractMindMapExportWizard;

/**
 * 
 * @author Karelun huang
 */
public class FlashExportWizard extends AbstractMindMapExportWizard {
	private static final String KEY_MINDMAP_FILE = "mindmapFile";
	private static final String KEY_JS_FILE = "jsFile";
	private static final String KEY_SWF_FILE = "swfFile";
	private static final String KEY_TITLE = "title";

	private static final String RESOURCES_POSTFIX = "_files";

	private static final String TEMPLATE_NAME = "flash.vm";
	private static final String JS_FILE_NAME = "flashobject.js";
	private static final String SWF_FILE_NAME = "visorFreemind.swf";

	private static final String TEMPLATE_ENCODING = "UTF-8";

	private static final String RESOURCE_LOCATION = "/html/";
	private static final String RESOURCE_HTML_TEMPLATE = RESOURCE_LOCATION + TEMPLATE_NAME;
	private static final String RESOURCE_SWF_FILE = RESOURCE_LOCATION + SWF_FILE_NAME;
	private static final String RESOURCE_JAVA_SCRIPT = RESOURCE_LOCATION + JS_FILE_NAME;

	private static final String SELECTION_NAME = "org.xmind.ui.export.html"; //$NON-NLS-1$

	private static final String MINDMAP_EXT = ".mm";
	public static final String HTML_EXT = ".html"; //$NON-NLS-1$

	private FreeMindExportWizardPage page;

	private String path;
	private String baseName;
	private String htmlFilename;

	private String resourcesDirectory;
	private String resourcesPath;
	private String mindmapFilename;

	public FlashExportWizard() {
		setWindowTitle(WizardMessages.FreeMindWizard_windowTitle);
		setDialogSettings(MindMapUIPlugin.getDefault().getDialogSettings(SELECTION_NAME));
		setDefaultPageImageDescriptor(MindMapUI.getImages().getWizBan(IMindMapImages.WIZ_EXPORT));
		initVelocityEngine();
	}

	protected void addValidPages() {
		addPage(page = new FreeMindExportWizardPage());
	}

	@Override
	protected void doExport(IProgressMonitor monitor, Display display, Shell parentShell)
			throws InvocationTargetException, InterruptedException {
		IMindMap mindMap = getSourceMindMap();
		String targetPath = getTargetPath();

		path = FilenameUtils.getFullPath(targetPath);
		baseName = FilenameUtils.getBaseName(targetPath);
		resourcesDirectory = baseName + RESOURCES_POSTFIX;
		resourcesPath = FilenameUtils.concat(path, resourcesDirectory);
		htmlFilename = FilenameUtils.getName(targetPath);
		mindmapFilename = FilenameUtils.getBaseName(htmlFilename) + MINDMAP_EXT;
		File path = new File(resourcesPath);
		path.mkdirs();

		saveHtml(mindMap);

		saveJavaScrpit();

		saveSwf();

		saveFreemind(monitor, display, parentShell, mindMap);
	}

	protected void saveFreemind(IProgressMonitor monitor, Display display, Shell parentShell, IMindMap mindMap)
			throws InvocationTargetException, InterruptedException {

		String mindmapPath = FilenameUtils.concat(resourcesPath, mindmapFilename);
		FreeMindExporter exporter = new FreeMindExporter(mindMap.getSheet(), mindmapPath);
		monitor.beginTask(null, 100);
		exporter.setMonitor(new SubProgressMonitor(monitor, 99));
		exporter.build();
		launchTargetFile(true, monitor, display, parentShell);
	}

	protected void saveJavaScrpit() {
		try {
			InputStream in = getBundle().getEntry(RESOURCE_JAVA_SCRIPT).openStream();
			String jsPath = FilenameUtils.concat(resourcesPath, JS_FILE_NAME);
			FileOutputStream out = new FileOutputStream(jsPath);
			IOUtils.copy(in, out);

		} catch (IOException e) {
			handleExportException(e);
		}
	}

	protected void saveSwf() {
		try {
			InputStream in = getBundle().getEntry(RESOURCE_SWF_FILE).openStream();
			String swfPath = FilenameUtils.concat(resourcesPath, SWF_FILE_NAME);
			FileOutputStream out = new FileOutputStream(swfPath);
			IOUtils.copy(in, out);

		} catch (IOException e) {
			handleExportException(e);
		}
	}

	protected void saveHtml(IMindMap mindMap) {
		VelocityEngine engine = initVelocityEngine();
		Template template = engine.getTemplate(TEMPLATE_NAME);
		VelocityContext context = new VelocityContext();

		context.put(KEY_TITLE, mindMap.getCentralTopic().getTitleText());

		context.put(KEY_JS_FILE, FilenameUtils.concat(resourcesDirectory, JS_FILE_NAME));

		String mindmapFile = FilenameUtils.concat(resourcesDirectory, mindmapFilename);
		context.put(KEY_MINDMAP_FILE, FilenameUtils.separatorsToUnix(mindmapFile));

		String swfFilename = FilenameUtils.concat(resourcesDirectory, SWF_FILE_NAME);
		context.put(KEY_SWF_FILE, FilenameUtils.separatorsToUnix(swfFilename));

		StringWriter writer = new StringWriter();
		template.merge(context, writer);

		try {
			FileWriter fw = new FileWriter(getTargetPath());
			fw.write(writer.toString());
			fw.close();
		} catch (IOException e) {
			handleExportException(e);
		}

	}

	@Override
	protected String getFormatName() {
		return WizardMessages.FreeMindWizard_formatName;
	}

	@Override
	protected boolean isExtensionCompatible(String path, String extension) {
		return super.isExtensionCompatible(path, extension) && HTML_EXT.equalsIgnoreCase(extension);
	}

	@Override
	protected void handleExportException(Throwable e) {
		super.handleExportException(e);
		page.setErrorMessage(e.getLocalizedMessage());
	}

	private VelocityEngine initVelocityEngine() {
		VelocityEngine engine = new VelocityEngine();
		Properties p = new Properties();

		p.setProperty("resource.loader", "string");
		p.setProperty("string.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
		engine.init(p);

		StringResourceRepository repository = StringResourceLoader.getRepository();
		repository.putStringResource(TEMPLATE_NAME, getHtmlTemplate());
		return (engine);
	}

	private String getHtmlTemplate() {
		try {

			InputStream in = getBundle().getEntry(RESOURCE_HTML_TEMPLATE).openStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(in, writer, TEMPLATE_ENCODING);
			return writer.toString();
		} catch (IOException e) {
			handleExportException(e);
			return null;
		}
	}

	private Bundle getBundle() {
		return FrameworkUtil.getBundle(this.getClass());
	}
}
