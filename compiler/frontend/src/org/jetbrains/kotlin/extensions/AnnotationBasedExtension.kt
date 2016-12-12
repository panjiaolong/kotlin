/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.TypeUtils

interface AnnotationBasedExtension {
    fun getAnnotationFqNames(modifierListOwner: KtModifierListOwner?): List<String>

    fun DeclarationDescriptor.hasSpecialAnnotation(modifierListOwner: KtModifierListOwner?): Boolean {
        if (annotations.any { it.isASpecialAnnotation(modifierListOwner) }) return true

        if (this is ClassDescriptor) {
            for (superType in TypeUtils.getAllSupertypes(defaultType)) {
                val superTypeDescriptor = superType.constructor.declarationDescriptor as? ClassDescriptor ?: continue
                if (superTypeDescriptor.annotations.any { it.isASpecialAnnotation(modifierListOwner) }) return true
            }
        }

        return false
    }

    private fun AnnotationDescriptor.isASpecialAnnotation(
            modifierListOwner: KtModifierListOwner?,
            allowMetaAnnotations: Boolean = true
    ): Boolean {
        val annotationType = type.constructor.declarationDescriptor ?: return false
        if (annotationType.fqNameSafe.asString() in getAnnotationFqNames(modifierListOwner)) return true

        if (allowMetaAnnotations) {
            for (metaAnnotation in annotationType.annotations) {
                if (metaAnnotation.isASpecialAnnotation(modifierListOwner, allowMetaAnnotations = false)) {
                    return true
                }
            }
        }

        return false
    }
}