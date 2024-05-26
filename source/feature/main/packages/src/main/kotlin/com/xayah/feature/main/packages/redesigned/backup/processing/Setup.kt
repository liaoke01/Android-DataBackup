package com.xayah.feature.main.packages.redesigned.backup.processing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.xayah.core.datastore.KeyResetBackupList
import com.xayah.core.model.StorageMode
import com.xayah.core.ui.component.Clickable
import com.xayah.core.ui.component.LocalSlotScope
import com.xayah.core.ui.component.PackageIcons
import com.xayah.core.ui.component.Selectable
import com.xayah.core.ui.component.Switchable
import com.xayah.core.ui.component.Title
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingTop
import com.xayah.core.ui.component.select
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.route.MainRoutes
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.LocalNavController
import com.xayah.core.ui.util.fromDrawable
import com.xayah.core.ui.util.fromString
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.ui.util.fromVector
import com.xayah.core.ui.util.getValue
import com.xayah.core.ui.util.icon
import com.xayah.core.ui.util.value
import com.xayah.feature.main.packages.R
import com.xayah.feature.main.packages.redesigned.ProcessingSetupScaffold
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalLayoutApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PagePackagesBackupProcessingSetup(localNavController: NavHostController, viewModel: IndexViewModel) {
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    LaunchedEffect(null) {
        viewModel.emitIntent(IndexUiIntent.UpdateApps)
    }

    ProcessingSetupScaffold(
        scrollBehavior = scrollBehavior,
        snackbarHostState = viewModel.snackbarHostState,
        title = StringResourceToken.fromStringId(R.string.setup),
        actions = {
            Button(
                enabled = uiState.storageType == StorageMode.Local || (uiState.cloudEntity != null && uiState.isTesting.not()),
                onClick = {
                    viewModel.emitIntentOnIO(IndexUiIntent.FinishSetup(navController = localNavController))
                }) {
                Text(text = StringResourceToken.fromStringId(R.string._continue).value)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        ) {
            val storageOptions = listOf(context.getString(R.string.local), context.getString(R.string.cloud))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .paddingHorizontal(SizeTokens.Level16)
                    .paddingTop(SizeTokens.Level16)
            ) {
                storageOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = storageOptions.size),
                        onClick = {
                            viewModel.emitStateOnMain(state = uiState.copy(storageIndex = index, storageType = if (index == 0) StorageMode.Local else StorageMode.Cloud))
                        },
                        selected = index == uiState.storageIndex
                    ) {
                        Text(label)
                    }
                }
            }

            Title(title = StringResourceToken.fromStringId(R.string.storage)) {
                AnimatedVisibility(uiState.storageIndex == 1) {
                    if (accounts.isEmpty()) {
                        Clickable(
                            title = StringResourceToken.fromStringId(R.string.account),
                            value = StringResourceToken.fromStringId(R.string.no_available_account),
                            leadingIcon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_cancel_circle),
                            trailingIcon = ImageVectorToken.fromVector(Icons.Rounded.KeyboardArrowRight),
                        ) {
                            navController.navigate(MainRoutes.Cloud.route)
                        }
                    } else {
                        val dialogState = LocalSlotScope.current!!.dialogSlot
                        var currentIndex by remember { mutableIntStateOf(if (uiState.cloudEntity == null) 0 else accounts.indexOfFirst { it.title.getValue(context) == uiState.cloudEntity!!.name }) }
                        Selectable(
                            title = StringResourceToken.fromStringId(R.string.account),
                            leadingIcon = uiState.cloudEntity?.type?.icon ?: ImageVectorToken.fromDrawable(R.drawable.ic_rounded_person),
                            value = if (uiState.cloudEntity == null) StringResourceToken.fromStringId(R.string.choose_an_account) else accounts[currentIndex].desc,
                            current = if (uiState.cloudEntity == null) StringResourceToken.fromStringId(R.string.not_selected) else accounts[currentIndex].title
                        ) {
                            viewModel.launchOnIO {
                                val selectedIndex = dialogState.select(
                                    title = StringResourceToken.fromStringId(R.string.account),
                                    defIndex = currentIndex,
                                    items = accounts
                                )
                                currentIndex = selectedIndex
                                viewModel.emitIntent(IndexUiIntent.SetCloudEntity(name = accounts[currentIndex].title.getValue(context)))
                            }
                        }
                    }
                }

                val interactionSource = remember { MutableInteractionSource() }
                Clickable(
                    title = StringResourceToken.fromStringId(R.string.apps),
                    value = StringResourceToken.fromString(uiState.packagesSize),
                    leadingIcon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_apps),
                    interactionSource = interactionSource,
                    content = {
                        PackageIcons(modifier = Modifier.paddingTop(SizeTokens.Level8), packages = uiState.packages, interactionSource = interactionSource) {}
                    }
                )
            }
            Title(title = StringResourceToken.fromStringId(R.string.backup_list)) {
                Switchable(
                    key = KeyResetBackupList,
                    title = StringResourceToken.fromStringId(R.string.reset_backup_list),
                    checkedText = StringResourceToken.fromStringId(R.string.reset_backup_list_desc),
                )
            }
        }
    }
}
