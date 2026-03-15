'use client'

import { useEffect } from 'react'
import * as CookieConsent from 'vanilla-cookieconsent'
import 'vanilla-cookieconsent/dist/cookieconsent.css'

export default function CookieConsentProvider() {
  useEffect(() => {
    CookieConsent.run({
      // Opt-in mode required for UK GDPR / PECR
      mode: 'opt-in',
      autoClearCookies: true, // Auto-clear cookies when user changes preferences

      guiOptions: {
        consentModal: {
          layout: 'bar',
          position: 'bottom',
        },
        preferencesModal: {
          layout: 'box',
        },
      },

      onConsent: ({ cookie }) => {
        // Fired on first consent
        if (CookieConsent.acceptedCategory('analytics')) {
          initClarity()
        }
      },

      onChange: ({ changedCategories }) => {
        // Fired when user updates preferences
        if (changedCategories.includes('analytics')) {
          if (CookieConsent.acceptedCategory('analytics')) {
            initClarity()
          } else {
            // Clarity doesn't have an opt-out method, so reload to clear it
            window.location.reload()
          }
        }
      },

      categories: {
        necessary: {
          enabled: true,
          readOnly: true, // User cannot disable necessary cookies
        },
        analytics: {
          enabled: false,
          autoClear: {
            cookies: [
              { name: /^_clck/ },  // Clarity
              { name: /^_clsk/ },  // Clarity
            ],
          },
        },
      },

      language: {
        default: 'en',
        translations: {
          en: {
            consentModal: {
              title: 'We use cookies',
              description:
                'We use essential cookies to keep TrustBridge running, and optional analytics cookies (Microsoft Clarity) to understand how you use the site. You can choose which to allow.',
              acceptAllBtn: 'Accept all',
              acceptNecessaryBtn: 'Reject all',
              showPreferencesBtn: 'Manage preferences',
            },
            preferencesModal: {
              title: 'Cookie preferences',
              acceptAllBtn: 'Accept all',
              acceptNecessaryBtn: 'Reject all',
              savePreferencesBtn: 'Save preferences',
              sections: [
                {
                  title: 'Necessary cookies',
                  description:
                    'These cookies are required for TrustBridge to function and cannot be disabled.',
                  linkedCategory: 'necessary',
                },
                {
                  title: 'Analytics cookies',
                  description:
                    'We use Microsoft Clarity to understand how visitors interact with our site. No personal data is sold or shared.',
                  linkedCategory: 'analytics',
                },
              ],
            },
          },
        },
      },
    })
  }, [])

  return null
}

function initClarity() {
  const projectId = process.env.NEXT_PUBLIC_CLARITY_PROJECT_ID
  if (!projectId || typeof window === 'undefined') return
  ;(function (c: any, l: any, a: any, r: any, i: any, t?: any, y?: any) {
    c[a] =
      c[a] ||
      function () {
        ;(c[a].q = c[a].q || []).push(arguments)
      }
    t = l.createElement(r)
    t.async = 1
    t.src = 'https://www.clarity.ms/tag/' + i
    y = l.getElementsByTagName(r)[0]
    y.parentNode.insertBefore(t, y)
  })(window, document, 'clarity', 'script', projectId)
}