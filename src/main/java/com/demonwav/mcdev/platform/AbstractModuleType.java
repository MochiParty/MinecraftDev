/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform;

import com.demonwav.mcdev.insight.generation.ui.EventGenerationPanel;
import com.demonwav.mcdev.util.McPsiUtil;
import com.intellij.codeInspection.ex.EntryPointsManager;
import com.intellij.codeInspection.ex.EntryPointsManagerBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizableStringList;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractModuleType<T extends AbstractModule> {

    @NotNull
    private final String groupId;
    @NotNull
    private final String artifactId;
    @NotNull
    protected final LinkedHashMap<String, Color> colorMap = new LinkedHashMap<>();

    public AbstractModuleType(@NotNull final String groupId, @NotNull final String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @NotNull
    @Contract(pure = true)
    public String getGroupId() {
        return groupId;
    }

    @NotNull
    @Contract(pure = true)
    public String getArtifactId() {
        return artifactId;
    }

    @Contract(pure = true)
    public abstract PlatformType getPlatformType();

    @Contract(pure = true)
    public abstract Icon getIcon();

    @Contract(pure = true)
    public boolean hasIcon() {
        return true;
    }

    @Contract(pure = true)
    public abstract String getId();

    @NotNull
    @Contract(pure = true)
    public abstract List<String> getIgnoredAnnotations();

    @NotNull
    @Contract(pure = true)
    public abstract List<String> getListenerAnnotations();

    @NotNull
    @Contract(pure = true)
    public Map<String, Color> getClassToColorMappings() {
        return this.colorMap;
    }

    @NotNull
    public abstract T generateModule(@NotNull Module module);

    public void performCreationSettingSetup(@NotNull Project project) {
        final JDOMExternalizableStringList annotations = ((EntryPointsManagerBase)EntryPointsManager.getInstance(project)).ADDITIONAL_ANNOTATIONS;
        getIgnoredAnnotations().stream().filter(annotation -> !annotations.contains(annotation)).forEach(annotations::add);
    }

    @NotNull
    @Contract(pure = true)
    public EventGenerationPanel getEventGenerationPanel(@NotNull PsiClass chosenClass) {
        return new EventGenerationPanel(chosenClass);
    }

    @Contract(pure = true)
    public boolean isEventGenAvailable() {
        return false;
    }

    @NotNull
    @Contract(pure = true)
    public String getDefaultListenerName(@NotNull PsiClass psiClass) {
        //noinspection ConstantConditions
        return "on" + psiClass.getName().replace("Event", "");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("groupId", groupId)
            .append("artifactId", artifactId)
            .toString();
    }

    /**
     * Given any PsiElement, determine if it resides in a module of this {@link AbstractModuleType}.
     *
     * @param element The element to check.
     * @return True if this element resides in a module of this type
     */
    public boolean isInModule(PsiElement element) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module == null) {
            return false;
        }

        MinecraftModule minecraftModule = MinecraftModule.getInstance(module);
        return minecraftModule != null && minecraftModule.isOfType(this);
    }

    protected String defaultNameForSubClassEvents(@NotNull PsiClass psiClass) {
        final boolean isInnerClass = !(psiClass.getParent() instanceof PsiFile);

        final StringBuilder name = new StringBuilder();
        if (isInnerClass) {
            final PsiClass containingClass = McPsiUtil.findContainingClass(psiClass.getParent());
            if (containingClass != null && containingClass.getName() != null) {
                name.append(containingClass.getName().replace("Event", ""));
            }
        }

        String className = psiClass.getName();
        if (className.startsWith(name.toString())) {
            className = className.substring(name.length());
        }
        name.append(className.replace("Event", ""));

        name.insert(0, "on");
        return name.toString();
    }
}
