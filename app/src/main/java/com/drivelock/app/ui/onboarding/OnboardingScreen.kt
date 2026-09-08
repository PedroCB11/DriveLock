package com.drivelock.app.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drivelock.app.R

@Composable
fun WelcomeScreen(onContinue: () -> Unit) = OnboardingScreen(
    step = 0,
    eyebrow = R.string.onboarding_welcome_eyebrow,
    title = R.string.onboarding_welcome_title,
    description = R.string.onboarding_welcome_description,
    action = R.string.onboarding_lets_go,
    onContinue = onContinue,
)

@Composable
fun HowItWorksScreen(onContinue: () -> Unit) = OnboardingScreen(
    step = 1,
    eyebrow = R.string.onboarding_how_eyebrow,
    title = R.string.onboarding_how_title,
    description = R.string.onboarding_how_description,
    action = R.string.continue_action,
    onContinue = onContinue,
)

@Composable
fun PrivacyScreen(onContinue: () -> Unit) = OnboardingScreen(
    step = 2,
    eyebrow = R.string.onboarding_privacy_eyebrow,
    title = R.string.onboarding_privacy_title,
    description = R.string.onboarding_privacy_description,
    action = R.string.onboarding_start,
    onContinue = onContinue,
)

@Composable
private fun OnboardingScreen(
    step: Int,
    @StringRes eyebrow: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes action: Int,
    onContinue: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(3) { index ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == step) 22.dp else 8.dp, 8.dp)
                            .background(
                                if (index == step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape,
                            ),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "D",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(eyebrow).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(stringResource(action), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
