/**
 */
package mmactivity;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Transition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link mmactivity.Transition#getSource <em>Source</em>}</li>
 *   <li>{@link mmactivity.Transition#getTarget <em>Target</em>}</li>
 * </ul>
 *
 * @see mmactivity.MmactivityPackage#getTransition()
 * @model
 * @generated
 */
public interface Transition extends EObject {
	/**
	 * Returns the value of the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' reference.
	 * @see #setSource(StateVertex)
	 * @see mmactivity.MmactivityPackage#getTransition_Source()
	 * @model required="true"
	 * @generated
	 */
	StateVertex getSource();

	/**
	 * Sets the value of the '{@link mmactivity.Transition#getSource <em>Source</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' reference.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(StateVertex value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target</em>' reference.
	 * @see #setTarget(StateVertex)
	 * @see mmactivity.MmactivityPackage#getTransition_Target()
	 * @model required="true"
	 * @generated
	 */
	StateVertex getTarget();

	/**
	 * Sets the value of the '{@link mmactivity.Transition#getTarget <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' reference.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(StateVertex value);

} // Transition
