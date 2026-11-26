package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.cs3.Cs3AndroidInstaller
import com.nuvio.app.features.cs3.Cs3AndroidRepositoryStore
import com.nuvio.app.features.cs3.Cs3RepositoryClient
import com.nuvio.app.features.cs3.Cs3RepositoryContent
import com.nuvio.app.features.cs3.createCs3Runtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.cloudstream_add_repository
import nuvio.composeapp.generated.resources.cloudstream_credit
import nuvio.composeapp.generated.resources.cloudstream_disclaimer
import nuvio.composeapp.generated.resources.cloudstream_extensions_section
import nuvio.composeapp.generated.resources.cloudstream_install
import nuvio.composeapp.generated.resources.cloudstream_installed
import nuvio.composeapp.generated.resources.cloudstream_load
import nuvio.composeapp.generated.resources.cloudstream_loading
import nuvio.composeapp.generated.resources.cloudstream_no_extensions
import nuvio.composeapp.generated.resources.cloudstream_remove
import nuvio.composeapp.generated.resources.cloudstream_repo_trust
import nuvio.composeapp.generated.resources.cloudstream_repo_url_label
import nuvio.composeapp.generated.resources.cloudstream_repositories_empty
import nuvio.composeapp.generated.resources.cloudstream_section_disclaimer
import nuvio.composeapp.generated.resources.cloudstream_section_repositories
import nuvio.composeapp.generated.resources.cloudstream_settings_title
import nuvio.composeapp.generated.resources.cloudstream_skipped_count
import nuvio.composeapp.generated.resources.cloudstream_uninstall
import org.jetbrains.compose.resources.stringResource

internal actual fun LazyListScope.cloudStreamSettingsContent(isTablet: Boolean) {
    item {
        val appContext = LocalContext.current.applicationContext
        remember(appContext) {
            Cs3AndroidInstaller.initialize(appContext)
            Cs3AndroidRepositoryStore.initialize(appContext)
        }
        val scope = rememberCoroutineScope()
        val runtimeNote = remember { createCs3Runtime().unsupportedReason }

        var repoUrls by remember { mutableStateOf(Cs3AndroidRepositoryStore.list().getOrDefault(emptyList())) }
        var urlInput by remember { mutableStateOf("") }
        var trusted by remember { mutableStateOf(false) }
        var installed by remember {
            mutableStateOf(Cs3AndroidInstaller.listInstalled().getOrDefault(emptyList()))
        }
        var selectedRepo by remember { mutableStateOf<String?>(null) }
        var content by remember { mutableStateOf<Cs3RepositoryContent?>(null) }
        var loading by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }

        SettingsSection(
            title = stringResource(Res.string.cloudstream_section_disclaimer),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsNavigationRow(
                    title = stringResource(Res.string.cloudstream_section_disclaimer),
                    description = stringResource(Res.string.cloudstream_disclaimer),
                    isTablet = isTablet,
                    onClick = {},
                )
                SettingsNavigationRow(
                    title = stringResource(Res.string.cloudstream_settings_title),
                    description = stringResource(Res.string.cloudstream_credit),
                    isTablet = isTablet,
                    onClick = {},
                )
            }
        }

        SettingsSection(
            title = stringResource(Res.string.cloudstream_section_repositories),
            isTablet = isTablet,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(stringResource(Res.string.cloudstream_repo_url_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = trusted, onCheckedChange = { trusted = it })
                    Text(stringResource(Res.string.cloudstream_repo_trust))
                }
                Button(
                    enabled = trusted && urlInput.isNotBlank(),
                    onClick = {
                        Cs3AndroidRepositoryStore.add(urlInput)
                            .onSuccess {
                                repoUrls = it
                                urlInput = ""
                                trusted = false
                                message = null
                            }
                            .onFailure { message = it.message }
                    },
                ) {
                    Text(stringResource(Res.string.cloudstream_add_repository))
                }

                if (repoUrls.isEmpty()) {
                    Text(stringResource(Res.string.cloudstream_repositories_empty))
                }
                repoUrls.forEach { url ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(url, modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                selectedRepo = url
                                content = null
                                loading = true
                                message = null
                                scope.launch {
                                    val result = Cs3RepositoryClient.fetch(url)
                                    loading = false
                                    result
                                        .onSuccess { content = it }
                                        .onFailure { message = it.message }
                                }
                            },
                        ) {
                            Text(stringResource(Res.string.cloudstream_load))
                        }
                        TextButton(
                            onClick = {
                                Cs3AndroidRepositoryStore.remove(url)
                                    .onSuccess {
                                        repoUrls = it
                                        if (selectedRepo == url) {
                                            selectedRepo = null
                                            content = null
                                        }
                                    }
                                    .onFailure { message = it.message }
                            },
                        ) {
                            Text(stringResource(Res.string.cloudstream_remove))
                        }
                    }
                }
                message?.let { Text(it) }
            }
        }

        if (selectedRepo != null) {
            SettingsSection(
                title = stringResource(Res.string.cloudstream_extensions_section),
                isTablet = isTablet,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (loading) {
                        Text(stringResource(Res.string.cloudstream_loading))
                    }
                    content?.let { loaded ->
                        if (loaded.skipped.isNotEmpty()) {
                            Text(stringResource(Res.string.cloudstream_skipped_count, loaded.skipped.size))
                        }
                        if (loaded.plugins.isEmpty()) {
                            Text(stringResource(Res.string.cloudstream_no_extensions))
                        }
                        loaded.plugins.forEach { plugin ->
                            val isInstalled = installed.any { it.internalName == plugin.internalName }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(plugin.name)
                                    Text("v${plugin.version}")
                                }
                                if (isInstalled) {
                                    Text(stringResource(Res.string.cloudstream_installed))
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                val result = withContext(Dispatchers.IO) {
                                                    Cs3AndroidInstaller.uninstall(plugin.internalName)
                                                }
                                                result
                                                    .onSuccess {
                                                        installed = Cs3AndroidInstaller.listInstalled()
                                                            .getOrDefault(emptyList())
                                                    }
                                                    .onFailure { message = it.message }
                                            }
                                        },
                                    ) {
                                        Text(stringResource(Res.string.cloudstream_uninstall))
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            val repo = selectedRepo ?: return@TextButton
                                            scope.launch {
                                                val result = withContext(Dispatchers.IO) {
                                                    Cs3AndroidInstaller.install(plugin, repo)
                                                }
                                                result
                                                    .onSuccess {
                                                        installed = Cs3AndroidInstaller.listInstalled()
                                                            .getOrDefault(emptyList())
                                                        message = null
                                                    }
                                                    .onFailure { message = it.message }
                                            }
                                        },
                                    ) {
                                        Text(stringResource(Res.string.cloudstream_install))
                                    }
                                }
                            }
                        }
                    }
                    runtimeNote?.let { Text(it) }
                }
            }
        }
    }
}
