import { SafeArea } from 'app/provider/safe-area'
import { NavigationProvider } from './navigation'

export function Provider({ children }: { children: React.ReactElement }) {
  return (
    <SafeArea>
      <NavigationProvider>{children}</NavigationProvider>
    </SafeArea>
  )
}
